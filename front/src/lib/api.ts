import { getAuthHeaders } from "./auth";

const API_BASE = import.meta.env.VITE_API_URL;

export interface PetOwner {
  name: string;
  number: number;
}

export interface Pet {
  id: string;
  name: string;
  type: "CAO" | "GATO";
  gender: "M" | "F";
  city: string;
  state: string;
  age: string;
  weight: string;
  race: string;
  imageUrl?: string;
  owner?: PetOwner;
}

export interface User {
  id: string;
  name: string;
  number: string;
  email: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
}

export interface PetFormData {
  name: string;
  type: string;
  gender: string;
  city: string;
  state: string;
  age: string;
  weight: string;
  race: string;
  image?: File | null;
}

export interface PageData<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const authHeaders = getAuthHeaders();

  const headers = new Headers({
    ...authHeaders,
    ...Object.fromEntries(
      new Headers(options.headers).entries()
    ),
  });

  const hasBody =
    options.body !== undefined &&
    options.body !== null;

  const isFormData =
    typeof FormData !== "undefined" &&
    options.body instanceof FormData;

  if (
    hasBody &&
    !isFormData &&
    !headers.has("Content-Type")
  ) {
    headers.set("Content-Type", "application/json");
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (res.status === 401 || res.status === 403) {
    const {
      getStoredAuth,
      isTokenExpired,
      clearAuth,
    } = await import("./auth");

    const stored = getStoredAuth();

    const json = await res
      .json()
      .catch(() => null);

    if (json?.message) {
      throw new Error(json.message);
    }

    if (!stored || isTokenExpired(stored.token)) {
      clearAuth();
      window.location.href = "/auth/login";

      throw new Error("Sessão expirada");
    }

    throw new Error(
      res.status === 403
        ? "Acesso negado"
        : "Não autorizado"
    );
  }

  if (res.status === 204) {
    return {
      success: true,
      data: null as T,
      message: "Operação realizada com sucesso",
      timestamp: new Date().toISOString(),
    };
  }

  const text = await res.text();

  let json: ApiResponse<T>;

  if (text.trim()) {
    try {
      json = JSON.parse(text);
    } catch {
      throw new Error("Resposta inválida da API");
    }
  } else {
    json = {
      success: res.ok,
      data: null as T,
      message: "",
      timestamp: new Date().toISOString(),
    };
  }

  if (!res.ok) {
    throw new Error(
      json.message || "Erro na requisição"
    );
  }

  return json;
}

export const petApi = {
  findAll: (page = 0, items = 9) =>
    request<PageData<Pet>>(
      `/pets?page=${page}&items=${items}`
    ),

  save: (data: PetFormData) => {
    const form = new FormData();

    const { image, ...rest } = data;

    Object.entries(rest).forEach(([key, value]) => {
      form.append(key, value ?? "");
    });

    if (image) {
      form.append("image", image);
    }

    return request<null>("/pets", {
      method: "POST",
      body: form,
    });
  },

  update: (id: string, data: PetFormData) => {
    const form = new FormData();

    const { image, ...rest } = data;

    Object.entries(rest).forEach(([key, value]) => {
      form.append(key, value ?? "");
    });

    if (image) {
      form.append("image", image);
    }

    return request<Pet>(`/pets/${id}`, {
      method: "PUT",
      body: form,
    });
  },

  delete: (id: string) =>
    request<null>(`/pets/${id}`, {
      method: "DELETE",
    }),

  search: (
    filter: Partial<PetFormData>,
    page = 0,
    items = 9
  ) =>
    request<PageData<Pet>>(
      `/pets/search?page=${page}&items=${items}`,
      {
        method: "POST",
        body: JSON.stringify(filter),
      }
    ),
};

export interface UserUpdateData {
  name: string;
  email: string;
  number: string;
}

export const userApi = {
  findAll: () =>
    request<User[]>("/users/admin"),

  update: (
    id: string,
    data: UserUpdateData
  ) =>
    request<User>(`/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string) =>
    request<null>(`/users/${id}`, {
      method: "DELETE",
    }),
};