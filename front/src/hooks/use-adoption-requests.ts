import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adoptionApi } from "@/lib/api";
import { toast } from "sonner";

export function useAdoptionNotifications(enabled: boolean) {
  return useQuery({
    queryKey: ["adoption-requests"],
    queryFn: async () => {
      const res = await adoptionApi.listMyNotifications();
      return res.data;
    },
    enabled,
    refetchInterval: enabled ? 30_000 : false,
  });
}

export function useRequestAdoption(onSuccess?: () => void) {
  return useMutation({
    mutationFn: (petId: string) => adoptionApi.request(petId),
    onSuccess: () => {
      toast.success("Pedido de adoção enviado! O tutor foi notificado.");
      onSuccess?.();
    },
    onError: (err: Error) => toast.error(err.message),
  });
}

export function useRespondAdoption() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, accept }: { id: string; accept: boolean }) =>
      accept ? adoptionApi.accept(id) : adoptionApi.reject(id),
    onSuccess: (_res, variables) => {
      queryClient.invalidateQueries({ queryKey: ["adoption-requests"] });
      toast.success(
        variables.accept
          ? "Pedido de adoção aceito!"
          : "Pedido de adoção recusado."
      );
    },
    onError: (err: Error) => toast.error(err.message),
  });
}
