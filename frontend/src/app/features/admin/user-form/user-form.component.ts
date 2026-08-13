import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';


@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  user: any = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    roleIds: []
  };
  
  isEditMode = false;
  loading = false;
  error = '';

  constructor(
    private adminService: AdminService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadUser(+id);
    }
  }

  loadUser(id: number): void {
    this.loading = true;
    this.adminService.getUserById(id).subscribe({
      next: (data) => {
        this.user = {
          ...data,
          roleIds: data.roles.map((r: any) => r.id)
        };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    const request = this.isEditMode
      ? this.adminService.updateUser(this.user.id, this.user)
      : this.adminService.createUser(this.user);

    request.subscribe({
      next: () => {
        this.router.navigate(['/admin/users']);
      },
      error: (err) => {
        this.error = 'Erreur lors de l enregistrement';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/users']);
  }
}
