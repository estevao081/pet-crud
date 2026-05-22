import { useState } from "react";
import { PetFormData } from "@/lib/api";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { ResponsiveSelect } from "@/components/ui/responsive-select";

const BRAZILIAN_STATES = [
  "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
  "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO",
];

interface PetFormProps {
  initialData?: Partial<PetFormData>;
  onSubmit: (data: PetFormData) => void;
  isPending?: boolean;
  submitLabel?: string;
  isEdit?: boolean;
}

export function PetForm({ initialData, onSubmit, isPending, submitLabel = "Salvar", isEdit = false }: PetFormProps) {
  const [form, setForm] = useState<PetFormData>({
    name: initialData?.name || "",
    type: initialData?.type || "",
    gender: initialData?.gender || "",
    city: initialData?.city || "",
    state: initialData?.state || "",
    age: initialData?.age || "",
    weight: initialData?.weight || "",
    race: initialData?.race || "",
    image: null,
  });
  const [preview, setPreview] = useState<string | null>(null);

  const handleChange = (field: keyof PetFormData, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleImageChange = (file: File | null) => {
    setForm((prev) => ({ ...prev, image: file }));
    setPreview(file ? URL.createObjectURL(file) : null);
  };


  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="name">Nome completo do pet</Label>
          <Input
            id="name"
            placeholder="Ex: Florzinha da Silva"
            value={form.name}
            onChange={(e) => handleChange("name", e.target.value)}
            required
          />
        </div>

        {!isEdit && (
          <div className="space-y-2">
            <Label htmlFor="type">Tipo</Label>
            <Select value={form.type} onValueChange={(v) => handleChange("type", v)}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione o tipo" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="CÃO">
                  <span className="flex items-center gap-2"><Dog className="w-4 h-4" /> Cão</span>
                </SelectItem>
                <SelectItem value="GATO">
                  <span className="flex items-center gap-2"><Cat className="w-4 h-4" /> Gato</span>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}

        {!isEdit && (
          <div className="space-y-2">
            <Label htmlFor="gender">Sexo</Label>
            <Select value={form.gender} onValueChange={(v) => handleChange("gender", v)}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione o sexo" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="M">Macho</SelectItem>
                <SelectItem value="F">Fêmea</SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}

        <div className="space-y-2">
          <Label htmlFor="race">Raça</Label>
          <Input
            id="race"
            placeholder="Ex: Siamês"
            value={form.race}
            onChange={(e) => handleChange("race", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="age">Idade (anos)</Label>
          <Input
            id="age"
            type="number"
            min="0"
            placeholder="Ex: 3"
            value={form.age}
            onChange={(e) => handleChange("age", e.target.value)}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="weight">Peso (kg)</Label>
          <Input
            id="weight"
            type="number"
            min="0"
            step="1"
            placeholder="Ex: 5"
            value={form.weight}
            onChange={(e) => handleChange("weight", e.target.value)}
          />
        </div>
      </div>

      <div className="space-y-3">
        <h3 className="font-display text-sm font-medium text-muted-foreground">Endereço onde foi encontrado</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="city">Cidade</Label>
            <Input
              id="city"
              placeholder="Ex: São Paulo"
              value={form.city}
              onChange={(e) => handleChange("city", e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="state">Estado</Label>
            <Select value={form.state} onValueChange={(v) => handleChange("state", v)}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione o estado" />
              </SelectTrigger>
              <SelectContent>
                {BRAZILIAN_STATES.map((uf) => (
                  <SelectItem key={uf} value={uf}>{uf}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="image">
          {isEdit ? "Trocar foto do pet (opcional)" : "Foto do pet"}
        </Label>
        <Input
          id="image"
          type="file"
          accept="image/*"
          onChange={(e) => handleImageChange(e.target.files?.[0] ?? null)}
          required={!isEdit}
        />
        {preview && (
          <img
            src={preview}
            alt="Pré-visualização"
            className="mt-2 h-32 w-32 rounded-md object-cover border"
          />
        )}
      </div>



      <Button type="submit" disabled={isPending} className="w-full sm:w-auto">
        {isPending ? "Salvando..." : submitLabel}
      </Button>
    </form>
  );
}
