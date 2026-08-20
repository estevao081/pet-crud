import { useState } from "react";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Search, X } from "lucide-react";
import { PetFormData } from "@/lib/api";

const BRAZILIAN_STATES = [
  "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
  "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO",
];

interface SearchBarProps {
  onSearch: (filter: Partial<PetFormData>) => void;
  onClear: () => void;
  isPending?: boolean;
}

export function SearchBar({ onSearch, onClear, isPending }: SearchBarProps) {
  const [name, setName] = useState("");
  const [type, setType] = useState("");
  const [gender, setGender] = useState("");
  const [race, setRace] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");

  const handleSearch = () => {
    const filter: Partial<PetFormData> = {};
    if (name.trim()) filter.name = name.trim();
    if (type && type !== "ALL") filter.type = type;
    if (gender && gender !== "ALL") filter.gender = gender;
    if (race.trim()) filter.race = race.trim();
    if (city.trim()) filter.city = city.trim();
    if (state && state !== "ALL") filter.state = state;
    onSearch(filter);
  };

  const handleClear = () => {
    setName("");
    setType("");
    setGender("");
    setRace("");
    setCity("");
    setState("");
    onClear();
  };

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="relative sm:col-span-2">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nome..."
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="pl-9"
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          />
        </div>
        <Input
          placeholder="Raça..."
          value={race}
          onChange={(e) => setRace(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        />
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-6 gap-3">
        <Select value={type} onValueChange={setType}>
          <SelectTrigger>
            <SelectValue placeholder="Tipo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            <SelectItem value="CÃO">Cão</SelectItem>
            <SelectItem value="GATO">Gato</SelectItem>
          </SelectContent>
        </Select>

        <Select value={gender} onValueChange={setGender}>
          <SelectTrigger>
            <SelectValue placeholder="Sexo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            <SelectItem value="M">Macho</SelectItem>
            <SelectItem value="F">Fêmea</SelectItem>
          </SelectContent>
        </Select>

        <Input
          placeholder="Cidade..."
          value={city}
          onChange={(e) => setCity(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        />

        <Select value={state} onValueChange={setState}>
          <SelectTrigger>
            <SelectValue placeholder="Estado" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {BRAZILIAN_STATES.map((uf) => (
              <SelectItem key={uf} value={uf}>{uf}</SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Button onClick={handleSearch} disabled={isPending} className="w-full">
          <Search className="h-4 w-4 mr-1.5" /> Buscar
        </Button>
        <Button variant="outline" onClick={handleClear} className="w-full">
          <X className="h-4 w-4 mr-1.5" /> Limpar
        </Button>
      </div>
    </div>
  );
}
