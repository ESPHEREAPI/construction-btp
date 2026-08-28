import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../core/services/order.service';
import { ProjectService } from '../../../core/services/project.service';
import { MaterialService } from '../../../core/services/material.service';
import { CurrencyService } from '../../../core/services/currency.service';
import { AppCurrencyPipe } from '../../../shared/pipes/app-currency.pipe';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AppCurrencyPipe],
  templateUrl: './order-form.component.html',
  styleUrls: ['./order-form.component.scss']
})
export class OrderFormComponent implements OnInit {
  order: any = {
    projectId: null,
    supplier: '',
    orderDate: new Date().toISOString().split('T')[0],
    expectedDeliveryDate: '',
    notes: '',
    items: [{ materialId: null, quantity: null, unitPrice: null, notes: '' }]
  };

  // Données de référence
  projects: any[] = [];
  materials: any[] = [];
  suppliers: string[] = [];

  // États de chargement
  isEditMode = false;
  loading = false;
  loadingData = true;
  loadingProjects = false;
  loadingMaterials = false;
  loadingSuppliers = false;

  // Messages
  error = '';
  successMessage = '';

  // UI
  showSuppliersList = false;
  maxDate: string;

  constructor(
    private orderService: OrderService,
    private projectService: ProjectService,
    private materialService: MaterialService,
    private currencyService: CurrencyService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Date maximale pour la commande (aujourd'hui)
    this.maxDate = new Date().toISOString().split('T')[0];
  }

  get currencyLabel(): string {
    return this.currencyService.current;
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  /**
   * Charger toutes les données nécessaires
   */
  loadInitialData(): void {
    this.loadingData = true;
    this.error = '';

    const id = this.route.snapshot.paramMap.get('id');

    // Charger projets, matériaux et fournisseurs en parallèle
    forkJoin({
      projects: this.projectService.getAll(),
      materials: this.materialService.getAll(),
      suppliers: this.orderService.getAll(undefined, undefined, 0, 100) // Charger plus de commandes pour les fournisseurs
    }).subscribe({
      next: (data: any) => {
        // Projets
        this.projects = data.projects.content || data.projects || [];
        console.log('✅ Projets chargés:', this.projects.length);

        // Matériaux
        this.materials = data.materials.content || data.materials || [];
        console.log('✅ Matériaux chargés:', this.materials.length);

        // Extraire les fournisseurs uniques des commandes existantes
        if (data.suppliers.content) {
          const allOrders = data.suppliers.content;
          const uniqueSuppliers = [...new Set(allOrders.map((order: any) => order.supplier))];
          this.suppliers = (uniqueSuppliers as string[])
            .filter((s) => typeof s === 'string' && s.trim() !== '');
        }
        console.log('✅ Fournisseurs chargés:', this.suppliers.length);

        this.loadingData = false;

        // Si mode édition, charger la commande
        if (id) {
          this.isEditMode = true;
          this.loadOrder(+id);
        }
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement des données:', err);
        this.error = 'Erreur lors du chargement des données. Veuillez réessayer.';
        this.loadingData = false;
      }
    });
  }

  /**
   * Charger une commande existante (mode édition)
   */
  loadOrder(id: number): void {
    this.loading = true;
    this.orderService.getById(id).subscribe({
      next: (data) => {
        console.log('✅ Commande chargée:', data);

        // Convertir les dates au format YYYY-MM-DD
        this.order = {
          ...data,
          orderDate: this.formatDate(data.orderDate),
          expectedDeliveryDate: this.formatDate(data.expectedDeliveryDate),
          items: data.items && data.items.length > 0
            ? data.items
            : [{ materialId: null, quantity: null, unitPrice: null, notes: '' }]
        };

        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement de la commande:', err);
        this.error = 'Erreur lors du chargement de la commande';
        this.loading = false;
      }
    });
  }

  /**
   * Formater une date pour l'input date
   */
  formatDate(date: any): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  /**
   * Ajouter un article à la commande
   */
  addItem(): void {
    this.order.items.push({
      materialId: null,
      quantity: null,
      unitPrice: null,
      notes: ''
    });
    console.log('➕ Article ajouté. Total:', this.order.items.length);
  }

  /**
   * Supprimer un article
   */
  removeItem(index: number): void {
    if (this.order.items.length > 1) {
      this.order.items.splice(index, 1);
      console.log('➖ Article supprimé. Total:', this.order.items.length);
    }
  }

  /**
   * Sélectionner un fournisseur depuis la liste
   */
  selectSupplier(supplier: string): void {
    this.order.supplier = supplier;
    this.showSuppliersList = false;
    console.log('✅ Fournisseur sélectionné:', supplier);
  }

  /**
   * Quand un matériau est sélectionné, remplir automatiquement le prix unitaire
   */
  onMaterialSelected(index: number): void {
    const item = this.order.items[index];
    if (item.materialId) {
      const material = this.getMaterialById(item.materialId);
      if (material && material.unitPrice) {
        item.unitPrice = material.unitPrice;
        this.calculateItemTotal(index);
        console.log('✅ Prix unitaire renseigné automatiquement:', material.unitPrice);
      }
    }
  }

  /**
   * Récupérer un matériau par son ID
   */
  getMaterialById(id: number): any {
    return this.materials.find(m => m.id === id);
  }

  /**
   * Calculer le total d'un article
   */
  calculateItemTotal(index: number): void {
    const item = this.order.items[index];
    if (item.quantity && item.unitPrice) {
      // Juste pour déclencher le recalcul visuel
      item.quantity = Number(item.quantity);
      item.unitPrice = Number(item.unitPrice);
    }
  }

  /**
   * Obtenir le total d'un article
   */
  getItemTotal(index: number): number {
    const item = this.order.items[index];
    if (item.quantity && item.unitPrice) {
      return Number(item.quantity) * Number(item.unitPrice);
    }
    return 0;
  }

  /**
   * Calculer le montant total de la commande
   */
  getTotalAmount(): number {
    return this.order.items.reduce((sum: number, item: any, index: number) => {
      return sum + this.getItemTotal(index);
    }, 0);
  }

  /**
   * Réinitialiser le formulaire
   */
  resetForm(): void {
    if (confirm('Êtes-vous sûr de vouloir réinitialiser le formulaire ?')) {
      this.order = {
        projectId: null,
        supplier: '',
        orderDate: new Date().toISOString().split('T')[0],
        expectedDeliveryDate: '',
        notes: '',
        items: [{ materialId: null, quantity: null, unitPrice: null, notes: '' }]
      };
      this.error = '';
      this.successMessage = '';
      console.log('🔄 Formulaire réinitialisé');
    }
  }

  /**
   * Soumettre le formulaire
   */
  onSubmit(): void {
    // Validation
    if (!this.order.projectId) {
      this.error = 'Veuillez sélectionner un projet';
      return;
    }

    if (!this.order.supplier || this.order.supplier.trim() === '') {
      this.error = 'Veuillez saisir un fournisseur';
      return;
    }

    if (!this.order.orderDate) {
      this.error = 'Veuillez saisir une date de commande';
      return;
    }

    if (!this.order.expectedDeliveryDate) {
      this.error = 'Veuillez saisir une date de livraison prévue';
      return;
    }

    if (this.order.items.length === 0) {
      this.error = 'Veuillez ajouter au moins un article';
      return;
    }

    // Valider chaque article
    for (let i = 0; i < this.order.items.length; i++) {
      const item = this.order.items[i];
      if (!item.materialId) {
        this.error = `Article ${i + 1}: Veuillez sélectionner un matériau`;
        return;
      }
      if (!item.quantity || item.quantity <= 0) {
        this.error = `Article ${i + 1}: Veuillez saisir une quantité valide`;
        return;
      }
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    console.log('📤 Envoi de la commande:', this.order);

    const request = this.isEditMode
      ? this.orderService.update(this.order.id, this.order)
      : this.orderService.create(this.order);

    request.subscribe({
      next: (response) => {
        console.log('✅ Commande enregistrée:', response);
        this.successMessage = this.isEditMode
          ? 'Commande modifiée avec succès !'
          : 'Commande créée avec succès !';

        // Rediriger après 1.5 secondes
        setTimeout(() => {
          this.router.navigate(['/orders']);
        }, 1500);
      },
      error: (err) => {
        console.error('❌ Erreur lors de l\'enregistrement:', err);
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement de la commande';
        this.loading = false;

        // Scroller vers le haut pour voir l'erreur
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  /**
   * Annuler et retourner à la liste
   */
  onCancel(): void {
    if (confirm('Êtes-vous sûr de vouloir annuler ? Les modifications seront perdues.')) {
      this.router.navigate(['/orders']);
    }
  }
}
