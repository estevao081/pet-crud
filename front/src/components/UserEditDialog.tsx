import { useEffect, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { toast } from "sonner";
import { User, userApi } from "@/lib/api";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const schema = z.object({
  name: z.string().trim().min(2, "Nome muito curto").max(100),
  email: z.string().trim().email("Email inválido").max(255),
  number: z
    .string()
    .trim()
    .regex(/^\d{10,11}$/, "Telefone deve conter 10 ou 11 dígitos"),
});

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
}

export function UserEditDialog({ open, onOpenChange, user }: Props) {
  const qc = useQueryClient();
  const [form, setForm] = useState({ name: "", email: "", number: "" });

  useEffect(() => {
    if (user) {
      setForm({
        name: user.name ?? "",
        email: user.email ?? "",
        number: (user.number ?? "").replace(/\D/g, ""),
      });
    }
  }, [user]);

  const mutation = useMutation({
    mutationFn: (id: string) => userApi.update(id, form),
    onSuccess: () => {
      toast.success("Usuário atualizado");
      qc.invalidateQueries({ queryKey: ["users", "admin"] });
      onOpenChange(false);
    },
    onError: (e: Error) => toast.error(e.message),
  });

  function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!user) return;
    const parsed = schema.safeParse(form);
    if (!parsed.success) {
      toast.error(parsed.error.issues[0].message);
      return;
    }
    mutation.mutate(user.id);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="font-display">Editar usuário</DialogTitle>
          <DialogDescription>Atualize nome, email e telefone.</DialogDescription>
        </DialogHeader>
        <form onSubmit={submit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Nome</Label>
            <Input
              id="name"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              maxLength={100}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              maxLength={255}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="number">Telefone (somente dígitos)</Label>
            <Input
              id="number"
              inputMode="numeric"
              value={form.number}
              onChange={(e) =>
                setForm({ ...form, number: e.target.value.replace(/\D/g, "") })
              }
              maxLength={11}
            />
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={mutation.isPending}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? "Salvando..." : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
