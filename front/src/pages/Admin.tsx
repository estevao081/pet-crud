import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Navigate, Link } from "react-router-dom";
import { User, userApi } from "@/lib/api";
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
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { ArrowLeft, Loader2, Pencil, ShieldCheck, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { UserEditDialog } from "@/components/UserEditDialog";

function formatPhone(raw: string): string {
  const digits = (raw ?? "").replace(/\D/g, "").padEnd(11, "0");
  return `(${digits.slice(0, 2)}) ${digits[2]}.${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}

export default function Admin() {
  const { user, isAuthenticated } = useAuth();
  const isAdmin = user?.role === "ROLE_ADMIN";
  const qc = useQueryClient();

  const [editing, setEditing] = useState<User | null>(null);
  const [deleting, setDeleting] = useState<User | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ["users", "admin"],
    queryFn: () => userApi.findAll(),
    enabled: isAuthenticated && isAdmin,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => userApi.delete(id),
    onSuccess: () => {
      toast.success("Usuário removido");
      qc.invalidateQueries({ queryKey: ["users", "admin"] });
      setDeleting(null);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  useEffect(() => {
    if (error) toast.error((error as Error).message);
  }, [error]);

  if (!isAuthenticated) return <Navigate to="/auth/login" replace />;
  if (!isAdmin) return <Navigate to="/" replace />;

  const users = [...(data?.data ?? [])].sort((a, b) => {
    if (a.role === b.role) return a.name.localeCompare(b.name);
    return a.role === "ROLE_ADMIN" ? -1 : 1;
  });

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
                  <TableHead>Role</TableHead>
                  <TableHead className="text-right">Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center text-muted-foreground py-10">
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
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setEditing(u)}
                            aria-label="Editar"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setDeleting(u)}
                            aria-label="Remover"
                            className="text-destructive hover:text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        )}
      </main>

      <UserEditDialog
        open={!!editing}
        onOpenChange={(o) => !o && setEditing(null)}
        user={editing}
      />

      <AlertDialog open={!!deleting} onOpenChange={(o) => !o && setDeleting(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="font-display">Remover usuário</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja remover <strong>{deleting?.name}</strong>? Esta ação
              não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteMutation.isPending}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deleting && deleteMutation.mutate(deleting.id)}
              disabled={deleteMutation.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleteMutation.isPending ? "Removendo..." : "Sim, remover"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
