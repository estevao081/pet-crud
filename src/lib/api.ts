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

async function request<T>(path: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const authHeaders = getAuthHeaders();
  const headers = new Headers({ ...authHeaders, ...Object.fromEntries(new Headers(options.headers).entries()) });
  const hasBody = options.body !== undefined && options.body !== null;

  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  if (hasBody && !isFormData && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }


  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (res.status === 401 || res.status === 403) {
    const { clearAuth } = await import("./auth");
    clearAuth();
    window.location.href = "/login";
    throw new Error("Sessão expirada");
  }

  const json = await res.json();
  if (!res.ok) throw new Error(json.message || "Erro na requisição");
  return json;
}

function toPayload(data: PetFormData) {
  const { image, ...rest } = data;
  return rest;
}

export const petApi = {
  findAll: (page = 0, items = 9) =>
    request<PageData<Pet>>(`/pets?page=${page}&items=${items}`),

  save: (data: PetFormData) => {
    const form = new FormData();
    const { image, ...rest } = data;
    Object.entries(rest).forEach(([k, v]) => form.append(k, v ?? ""));
    if (image) form.append("image", image);
    return request<null>("/pets", { method: "POST", body: form });
  },

  update: (id: string, data: PetFormData) =>
    request<Pet>(`/pets/${id}`, { method: "PUT", body: JSON.stringify(toPayload(data)) }),

  delete: (id: string) =>
    request<null>(`/pets/${id}`, { method: "DELETE" }),

  search: (filter: Partial<PetFormData>, page = 0, items = 9) =>
    request<PageData<Pet>>(`/pets/search?page=${page}&items=${items}`, {
      method: "POST",
      body: JSON.stringify(filter),
    }),
};

