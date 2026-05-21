import { useEffect, useState } from "react";
import { User, UserUpdateData } from "@/lib/api";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
  onSubmit: (data: UserUpdateData) => void;
  isPending?: boolean;
}

export function UserEditDialog({ open, onOpenChange, user, onSubmit, isPending }: Props) {
  const [form, setForm] = useState<UserUpdateData>({
    name: "",
    number: "",
    email: "",
    password: "",
  });

  useEffect(() => {
    if (user) {
      setForm({ name: user.name, number: user.number, email: user.email, password: "" });
    }
  }, [user]);

  const handle = (k: keyof UserUpdateData, v: string) =>
    setForm((p) => ({ ...p, [k]: v }));

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="font-display">Editar usuário</DialogTitle>
          <DialogDescription>
            Atualize informações de <strong>{user?.name}</strong>.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={submit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="u-name">Nome</Label>
            <Input id="u-name" value={form.name} onChange={(e) => handle("name", e.target.value)} required />
          </div>
          <div className="space-y-2">
            <Label htmlFor="u-email">Email</Label>
            <Input id="u-email" type="email" value={form.email} onChange={(e) => handle("email", e.target.value)} required />
          </div>
          <div className="space-y-2">
            <Label htmlFor="u-number">Telefone</Label>
            <Input id="u-number" value={form.number} onChange={(e) => handle("number", e.target.value)} required />
          </div>
          <div className="space-y-2">
            <Label htmlFor="u-pass">Nova senha</Label>
            <Input id="u-pass" type="password" value={form.password} onChange={(e) => handle("password", e.target.value)} required />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? "Salvando..." : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
