import { useState } from "react";
import { Pet, PetFormData } from "@/lib/api";
import { usePets, useSearchPets, useSavePet, useUpdatePet, useDeletePet } from "@/hooks/use-pets";
import { useAuth } from "@/contexts/AuthContext";
import { PetCard } from "@/components/PetCard";
import { SearchBar } from "@/components/SearchBar";
import { PetDialog } from "@/components/PetDialog";
import { DeleteConfirmDialog } from "@/components/DeleteConfirmDialog";
import { Button } from "@/components/ui/button";
import { PawPrint, Plus, Loader2, LogIn, LogOut, UserPlus, ChevronLeft, ChevronRight, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { toast } from "sonner";

export default function Index() {
  const [page, setPage] = useState(0);
  const [searchFilter, setSearchFilter] = useState<Partial<PetFormData> | null>(null);

  const { data: petsPage, isLoading, refetch } = usePets(page);
  const searchMutation = useSearchPets();
  const { user, isAuthenticated, logout } = useAuth();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingPet, setEditingPet] = useState<Pet | null>(null);
  const [deletingPet, setDeletingPet] = useState<Pet | null>(null);

  const saveMutation = useSavePet(() => setDialogOpen(false));
  const updateMutation = useUpdatePet(() => {
    setDialogOpen(false);
    setEditingPet(null);
  });
  const deleteMutation = useDeletePet();

  const isSearching = !!searchFilter;
  const searchData = searchMutation.data?.data;
  const activePage = isSearching ? searchData : petsPage;
  const pets = activePage?.content ?? [];
  const totalPages = activePage?.totalPages ?? 0;
  const currentPage = activePage?.number ?? 0;

  const handleEdit = (pet: Pet) => {
    setEditingPet(pet);
    setDialogOpen(true);
  };

  const handleNewPet = () => {
    if (!isAuthenticated) {
      toast.error("Faça login para cadastrar um pet.");
      return;
    }
    setEditingPet(null);
    setDialogOpen(true);
  };

  const handleSubmit = (data: PetFormData) => {
    if (editingPet) {
      updateMutation.mutate({ id: editingPet.id, data });
    } else {
      saveMutation.mutate(data);
    }
  };

  const handleDeleteConfirm = () => {
    if (deletingPet) {
      deleteMutation.mutate(deletingPet.id, {
        onSuccess: () => setDeletingPet(null),
      });
    }
  };

  const handleSearch = (filter: Partial<PetFormData>) => {
    setSearchFilter(filter);
    searchMutation.mutate({ filter, page: 0 });
  };

  const handleClearSearch = () => {
    setSearchFilter(null);
    refetch();
  };

  const handlePageChange = (newPage: number) => {
    if (isSearching && searchFilter) {
      searchMutation.mutate({ filter: searchFilter, page: newPage });
    } else {
      setPage(newPage);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b bg-card/80 backdrop-blur-sm sticky top-0 z-10">
        <div className="container flex items-center justify-between h-16">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <PawPrint className="h-5 w-5" />
            </div>
            <h1 className="font-display text-xl font-bold tracking-tight">
              AdotaPet
            </h1>
          </div>

          <div className="flex items-center gap-1.5 sm:gap-2">
            {isAuthenticated ? (
              <>
                <span className="text-sm text-muted-foreground hidden sm:inline">
                  Olá, <strong className="text-foreground">{user?.name}</strong>
                </span>
                <Button size="sm" onClick={handleNewPet} className="sm:size-default">
                  <Plus className="h-4 w-4 sm:mr-1.5" /> <span className="hidden sm:inline">Novo Pet</span>
                </Button>
                {user?.role === "ROLE_ADMIN" && (
                  <Button variant="outline" size="sm" asChild>
                    <Link to="/admin">
                      <ShieldCheck className="h-4 w-4 sm:mr-1.5" /> <span className="hidden sm:inline">Admin</span>
                    </Link>
                  </Button>
                )}
                <Button variant="outline" size="icon" onClick={logout} title="Sair" className="h-9 w-9 sm:h-10 sm:w-10">
                  <LogOut className="h-4 w-4" />
                </Button>
              </>
            ) : (
              <>
                <Button variant="outline" size="sm" asChild>
                  <Link to="/auth/login">
                    <LogIn className="h-4 w-4 sm:mr-1.5" /> <span className="hidden sm:inline">Entrar</span>
                  </Link>
                </Button>
                <Button size="sm" asChild>
                  <Link to="/auth/register">
                    <UserPlus className="h-4 w-4 sm:mr-1.5" /> <span className="hidden sm:inline">Cadastrar</span>
                  </Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="container py-8 space-y-6">
        <div className="text-center space-y-2 pb-2">
          <h2 className="font-display text-3xl sm:text-4xl font-bold">
            Sistema de Adoção de Pets 🐾
          </h2>
          <p className="text-muted-foreground max-w-md mx-auto">
            Cadastre ou busque pets para encontrar um lar para cada um.
          </p>
        </div>

        <SearchBar
          onSearch={handleSearch}
          onClear={handleClearSearch}
          isPending={searchMutation.isPending}
        />

        {isLoading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : pets.length === 0 ? (
          <div className="text-center py-20 space-y-3">
            <PawPrint className="h-12 w-12 mx-auto text-muted-foreground/40" />
            <p className="text-muted-foreground">Nenhum pet cadastrado ainda.</p>
            {isAuthenticated && (
              <Button variant="outline" onClick={handleNewPet}>
                <Plus className="h-4 w-4 mr-1.5" /> Cadastrar primeiro pet
              </Button>
            )}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {pets.map((pet) => (
                <PetCard
                  key={pet.id}
                  pet={pet}
                  onEdit={handleEdit}
                  onDelete={setDeletingPet}
                  currentUserName={user?.name}
                  isAdmin={user?.role === "ROLE_ADMIN"}
                />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 pt-4">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage === 0}
                  onClick={() => handlePageChange(currentPage - 1)}
                >
                  <ChevronLeft className="h-4 w-4 mr-1" /> Anterior
                </Button>
                <span className="text-sm text-muted-foreground px-3">
                  Página <strong className="text-foreground">{currentPage + 1}</strong> de {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => handlePageChange(currentPage + 1)}
                >
                  Próxima <ChevronRight className="h-4 w-4 ml-1" />
                </Button>
              </div>
            )}
          </>
        )}
      </main>

      <PetDialog
        open={dialogOpen}
        onOpenChange={(open) => {
          setDialogOpen(open);
          if (!open) setEditingPet(null);
        }}
        pet={editingPet}
        onSubmit={handleSubmit}
        isPending={saveMutation.isPending || updateMutation.isPending}
      />

      <DeleteConfirmDialog
        open={!!deletingPet}
        onOpenChange={(open) => !open && setDeletingPet(null)}
        pet={deletingPet}
        onConfirm={handleDeleteConfirm}
        isPending={deleteMutation.isPending}
      />
    </div>
  );
}
