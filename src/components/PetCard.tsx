import { Pet } from "@/lib/api";
import { Dog, Cat, MapPin, Scale, Calendar, Trash2, Pencil, User } from "lucide-react";

function formatPhone(raw: string): string {
  const digits = raw.replace(/\D/g, "").padEnd(11, "0");
  return `(${digits.slice(0, 2)}) ${digits[2]}.${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

interface PetCardProps {
  pet: Pet;
  onEdit: (pet: Pet) => void;
  onDelete: (pet: Pet) => void;
  currentUserName?: string;
  isAdmin?: boolean;
}

export function PetCard({ pet, onEdit, onDelete, currentUserName, isAdmin }: PetCardProps) {
  const Icon = pet.type === "CAO" ? Dog : Cat;
  const typeLabel = pet.type === "CAO" ? "Cão" : "Gato";
  const genderLabel = pet.gender === "M" ? "Macho" : "Fêmea";

  const isOwner = currentUserName && pet.owner?.name === currentUserName;
  const canManage = isOwner || isAdmin;

  return (
    <div className="group relative rounded-lg border bg-card p-5 shadow-card hover:shadow-elevated transition-all duration-200 animate-fade-in">
      {pet.imageUrl && (
        <img
          src={pet.imageUrl}
          alt={pet.name}
          loading="lazy"
          className="mb-4 aspect-[9/16] w-full rounded-md object-cover"
        />
      )}
      <div className="flex items-start justify-between gap-3">

        <div className="flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-primary/10 text-primary">
            <Icon className="h-5 w-5" />
          </div>
          <div>
            <h3 className="font-display text-lg font-semibold leading-tight">{pet.name}</h3>
            <div className="flex items-center gap-2 mt-1">
              <Badge variant="secondary" className="text-xs">{typeLabel}</Badge>
              <Badge variant="outline" className="text-xs">{genderLabel}</Badge>
            </div>
          </div>
        </div>

        {canManage && (
          <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => onEdit(pet)}>
              <Pencil className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive hover:text-destructive" onClick={() => onDelete(pet)}>
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        )}
      </div>

      <div className="mt-4 grid grid-cols-2 gap-2 text-sm text-muted-foreground">
        <div className="flex items-center gap-1.5">
          <Calendar className="h-3.5 w-3.5" />
          <span>{pet.age && !isNaN(Number(pet.age)) ? `${pet.age} anos` : "não informado"}</span>
        </div>
        <div className="flex items-center gap-1.5">
          <Scale className="h-3.5 w-3.5" />
          <span>{pet.weight && !isNaN(Number(pet.weight)) ? `${pet.weight} kg` : "não informado"}</span>
        </div>
        <div className="flex items-center gap-1.5 col-span-2">
          <MapPin className="h-3.5 w-3.5 shrink-0" />
          <span className="truncate">
            {pet.city || pet.state
              ? `${pet.city}${pet.state ? ` - ${pet.state}` : ""}`
              : "não informado"}
          </span>
        </div>
      </div>

      {pet.race && pet.race !== "não informado" && (
        <p className="mt-3 text-xs text-muted-foreground">
          Raça: <span className="font-medium text-foreground">{pet.race}</span>
        </p>
      )}

      {pet.owner && (
        <div className="mt-3 flex items-center gap-1.5 text-xs text-muted-foreground border-t pt-3">
          <User className="h-3.5 w-3.5 shrink-0" />
          <div className="flex flex-col">
            <span>Cadastrado por <strong className="text-foreground">{pet.owner.name}</strong></span>
            <span>{formatPhone(String(pet.owner.number))}</span>
          </div>
        </div>
      )}
    </div>
  );
}
