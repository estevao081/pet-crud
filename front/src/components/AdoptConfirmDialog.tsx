import { Pet } from "@/lib/api";
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

interface AdoptConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pet: Pet | null;
  onConfirm: () => void;
  isPending?: boolean;
}

export function AdoptConfirmDialog({
  open,
  onOpenChange,
  pet,
  onConfirm,
  isPending,
}: AdoptConfirmDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle className="font-display">Confirmar pedido de adoção</AlertDialogTitle>
          <AlertDialogDescription>
            Deseja enviar um pedido de adoção para <strong>{pet?.name}</strong>?
            O tutor atual receberá uma notificação com seu nome, telefone e
            endereço, e poderá aceitar ou recusar o pedido.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>Cancelar</AlertDialogCancel>
          <AlertDialogAction onClick={onConfirm} disabled={isPending}>
            {isPending ? "Enviando..." : "Sim, quero adotar"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
