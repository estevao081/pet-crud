import { Bell, Check, X, MapPin, Phone, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { useAdoptionNotifications, useRespondAdoption } from "@/hooks/use-adoption-requests";

function formatPhone(raw: string): string {
  const digits = (raw ?? "").replace(/\D/g, "").padEnd(11, "0");
  return `(${digits.slice(0, 2)}) ${digits[2]}.${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
}

interface AdoptionNotificationsProps {
  enabled: boolean;
}

export function AdoptionNotifications({ enabled }: AdoptionNotificationsProps) {
  const { data: requests } = useAdoptionNotifications(enabled);
  const respondMutation = useRespondAdoption();

  const all = requests ?? [];
  const pending = all.filter((r) => r.status === "PENDING");

  if (!enabled) return null;

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant="outline" size="icon" className="relative h-9 w-9 sm:h-10 sm:w-10" title="Notificações de adoção">
          <Bell className="h-4 w-4" />
          {pending.length > 0 && (
            <span className="absolute -top-1.5 -right-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-semibold text-destructive-foreground">
              {pending.length}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-80 p-0">
        <div className="border-b px-4 py-3">
          <h3 className="font-display text-sm font-semibold">Pedidos de adoção</h3>
        </div>
        <ScrollArea className="max-h-96">
          {all.length === 0 ? (
            <p className="px-4 py-8 text-center text-sm text-muted-foreground">
              Nenhum pedido de adoção por aqui ainda.
            </p>
          ) : (
            <div className="divide-y">
              {all.map((req) => (
                <div key={req.id} className="p-4 space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <p className="text-sm">
                      <strong className="text-foreground">{req.requesterName}</strong>{" "}
                      quer adotar <strong className="text-foreground">{req.petName}</strong>
                    </p>
                    {req.status !== "PENDING" && (
                      <Badge variant={req.status === "ACCEPTED" ? "default" : "secondary"} className="shrink-0 text-xs">
                        {req.status === "ACCEPTED" ? "Aceito" : "Recusado"}
                      </Badge>
                    )}
                  </div>

                  <div className="space-y-1 text-xs text-muted-foreground">
                    <div className="flex items-center gap-1.5">
                      <User className="h-3 w-3 shrink-0" />
                      <span>{req.requesterName}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Phone className="h-3 w-3 shrink-0" />
                      <span>{formatPhone(req.requesterPhone)}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <MapPin className="h-3 w-3 shrink-0" />
                      <span className="truncate">{req.requesterAddress}</span>
                    </div>
                  </div>

                  {req.status === "PENDING" && (
                    <div className="flex gap-2 pt-1">
                      <Button
                        size="sm"
                        className="flex-1"
                        disabled={respondMutation.isPending}
                        onClick={() => respondMutation.mutate({ id: req.id, accept: true })}
                      >
                        <Check className="h-3.5 w-3.5 mr-1" /> Aceitar
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="flex-1"
                        disabled={respondMutation.isPending}
                        onClick={() => respondMutation.mutate({ id: req.id, accept: false })}
                      >
                        <X className="h-3.5 w-3.5 mr-1" /> Recusar
                      </Button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </ScrollArea>
      </PopoverContent>
    </Popover>
  );
}
