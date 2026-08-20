import { Pet, PetFormData } from "@/lib/api";
import { PetForm } from "./PetForm";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

interface PetDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pet?: Pet | null;
  onSubmit: (data: PetFormData) => void;
  isPending?: boolean;
}

export function PetDialog({ open, onOpenChange, pet, onSubmit, isPending }: PetDialogProps) {
  const isEdit = !!pet;

  const initialData: Partial<PetFormData> | undefined = pet
    ? {
        name: pet.name,
        type: pet.type === "CAO" ? "CÃO" : "GATO",
        gender: pet.gender,
        city: pet.city || "",
        state: pet.state || "",
        age: pet.age,
        weight: pet.weight,
        race: pet.race,
      }
    : undefined;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="font-display text-xl">
            {isEdit ? "Editar Pet" : "Cadastrar Novo Pet"}
          </DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Atualize as informações do pet abaixo."
              : "Preencha os dados do pet para cadastrá-lo no abrigo."}
          </DialogDescription>
        </DialogHeader>
        <PetForm
          key={pet?.id || "new"}
          initialData={initialData}
          onSubmit={onSubmit}
          isPending={isPending}
          submitLabel={isEdit ? "Atualizar" : "Cadastrar"}
          isEdit={isEdit}
        />
      </DialogContent>
    </Dialog>
  );
}
