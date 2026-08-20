import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from "react";
import {
  type AuthUser,
  type RegisterData,
  type LoginData,
  registerUser,
  loginUser,
  getStoredAuth,
  storeAuth,
  clearAuth,
  isTokenExpired,
} from "@/lib/auth";
import { toast } from "sonner";

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (data: LoginData) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = getStoredAuth();
    if (stored && isTokenExpired(stored.token)) {
      clearAuth();
      return null;
    }
    return stored;
  });

  const logout = useCallback(() => {
    clearAuth();
    setUser(null);
  }, []);

  // Verifica expiração token a cada 30s
  useEffect(() => {
    const interval = setInterval(() => {
      const stored = getStoredAuth();
      if (stored && isTokenExpired(stored.token)) {
        logout();
        toast.error("Sessão expirada. Faça login novamente.");
      }
    }, 30_000);
    return () => clearInterval(interval);
  }, [logout]);

  const login = useCallback(async (data: LoginData) => {
    const authUser = await loginUser(data);
    storeAuth(authUser);
    setUser(authUser);
  }, []);

  const register = useCallback(async (data: RegisterData) => {
    const authUser = await registerUser(data);
    storeAuth(authUser);
    setUser(authUser);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
