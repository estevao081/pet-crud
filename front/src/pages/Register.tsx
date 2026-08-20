import { useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { PawPrint, Loader2, ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [number, setNumber] = useState("");
  const [isPending, setIsPending] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsPending(true);
    try {
      await register({ name, email, password, number });
      toast.success("Conta criada com sucesso!");
      navigate("/");
    } catch (err: any) {
      toast.error(err.message || "Erro ao cadastrar");
    } finally {
      setIsPending(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center space-y-2">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground mx-auto">
            <PawPrint className="h-6 w-6" />
          </div>
          <h1 className="font-display text-2xl font-bold">Criar Conta</h1>
          <p className="text-muted-foreground text-sm">
            Cadastre-se para gerenciar pets no AdotaPet.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border bg-card p-6 shadow-card">
          <div className="space-y-2">
            <Label htmlFor="name">Nome</Label>
            <Input
              id="name"
              placeholder="Seu nome completo"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              placeholder="seu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Senha</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="number">Telefone</Label>
            <Input
              id="number"
              type="tel"
              placeholder="(11) 99999-9999"
              value={number}
              onChange={(e) => setNumber(e.target.value)}
              required
            />
          </div>
          <Button type="submit" disabled={isPending} className="w-full">
            {isPending ? <Loader2 className="h-4 w-4 animate-spin mr-1.5" /> : null}
            {isPending ? "Cadastrando..." : "Cadastrar"}
          </Button>
        </form>

        <p className="text-center text-sm text-muted-foreground">
          Já tem conta?{" "}
          <Link to="/auth/login" className="text-primary font-medium hover:underline">
            Faça login
          </Link>
        </p>

        <div className="text-center">
          <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-3.5 w-3.5" /> Voltar para início
          </Link>
        </div>
      </div>
    </div>
  );
}
