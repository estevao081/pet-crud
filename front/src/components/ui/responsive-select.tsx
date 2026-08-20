import * as React from "react";
import { useIsMobile } from "@/hooks/use-mobile";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { ChevronDown } from "lucide-react";

export interface ResponsiveSelectOption {
  value: string;
  label: string;
  node?: React.ReactNode;
}

interface ResponsiveSelectProps {
  value: string;
  onValueChange: (value: string) => void;
  placeholder?: string;
  options: ResponsiveSelectOption[];
  className?: string;
  id?: string;
}

/**
 * Em mobile usa <select> nativo para evitar bugs do Radix Select
 * em Chrome Android (tela em branco / pointer-events travados) em
 * alguns dispositivos (ex.: Galaxy A07, A31).
 */
export function ResponsiveSelect({
  value,
  onValueChange,
  placeholder,
  options,
  className,
  id,
}: ResponsiveSelectProps) {
  const isMobile = useIsMobile();

  if (isMobile) {
    return (
      <div className={cn("relative", className)}>
        <select
          id={id}
          value={value}
          onChange={(e) => onValueChange(e.target.value)}
          className={cn(
            "flex h-10 w-full appearance-none items-center justify-between rounded-md border border-input bg-background pl-3 pr-9 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
            !value && "text-muted-foreground"
          )}
          style={{ fontSize: "16px" }}
        >
          {placeholder && (
            <option value="" disabled hidden>
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <ChevronDown className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 opacity-50" />
      </div>
    );
  }

  return (
    <Select value={value} onValueChange={onValueChange}>
      <SelectTrigger id={id} className={className}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        {options.map((opt) => (
          <SelectItem key={opt.value} value={opt.value}>
            {opt.node ?? opt.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
