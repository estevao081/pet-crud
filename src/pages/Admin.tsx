import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { Navigate, Link } from "react-router-dom";
import { userApi } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ArrowLeft, Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";

function formatPhone(raw: string): string {
  const digits = (raw ?? "").replace(/\D/g, "").padEnd(11, "0");
  return `(${digits.slice(0, 2)}) ${digits[2]}.${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}

export default function Admin() {
  const { user, isAuthenticated } = useAuth();
  const isAdmin = user?.role === "ROLE_ADMIN";

  const { data, isLoading, error } = useQuery({
    queryKey: ["users", "admin"],
    queryFn: () => userApi.findAll(),
    enabled: isAuthenticated && isAdmin,
  });

  useEffect(() => {
    if (error) toast.error((error as Error).message);
  }, [error]);

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/" replace />;

  const users = data?.data ?? [];

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b bg-card/80 backdrop-blur-sm sticky top-0 z-10">
        <div className="container flex items-center justify-between h-16">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <h1 className="font-display text-xl font-bold tracking-tight">
              Administração
            </h1>
          </div>
          <Button variant="outline" size="sm" asChild>
            <Link to="/">
              <ArrowLeft className="h-4 w-4 mr-1.5" /> Voltar
            </Link>
          </Button>
        </div>
      </header>

      <main className="container py-8 space-y-6">
        <div className="space-y-1">
          <h2 className="font-display text-2xl sm:text-3xl font-bold">Usuários cadastrados</h2>
          <p className="text-muted-foreground text-sm">
            Total: <strong className="text-foreground">{users.length}</strong>
          </p>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="rounded-lg border bg-card shadow-card overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nome</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Telefone</TableHead>
                  <TableHead>Papel</TableHead>
                  <TableHead className="hidden md:table-cell">ID</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center text-muted-foreground py-10">
                      Nenhum usuário encontrado.
                    </TableCell>
                  </TableRow>
                ) : (
                  users.map((u) => (
                    <TableRow key={u.id}>
                      <TableCell className="font-medium">{u.name}</TableCell>
                      <TableCell>{u.email}</TableCell>
                      <TableCell>{formatPhone(u.number)}</TableCell>
                      <TableCell>
                        <Badge variant={u.role === "ROLE_ADMIN" ? "default" : "secondary"}>
                          {u.role === "ROLE_ADMIN" ? "Admin" : "Usuário"}
                        </Badge>
                      </TableCell>
                      <TableCell className="hidden md:table-cell text-xs text-muted-foreground">
                        {u.id}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        )}
      </main>
    </div>
  );
}
