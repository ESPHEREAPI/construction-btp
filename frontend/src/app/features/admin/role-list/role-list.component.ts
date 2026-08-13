import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss']
})
export class RoleListComponent implements OnInit {
  roles: any[] = [];
  loading = false;

  constructor() {}

  ngOnInit(): void {
    // TODO: Implémenter la récupération des rôles
    this.roles = [
      { id: 1, name: 'ROLE_ADMIN', description: 'Administrateur', permissions: [] },
      { id: 2, name: 'ROLE_MANAGER', description: 'Gestionnaire', permissions: [] },
      { id: 3, name: 'ROLE_USER', description: 'Utilisateur', permissions: [] }
    ];
  }
}
