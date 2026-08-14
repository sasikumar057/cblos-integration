import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  isLoading = signal(false);
  errorMessage = signal('');

  isStaffWorkspace(): boolean {
    return this.router.url.startsWith('/corporate-login');
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    const { email, password } = this.loginForm.value;
    this.authService.login({ username: email!, password: password! }).subscribe({
      next: (user) => {
        if (this.isStaffWorkspace() && user.role === 'CUSTOMER') {
          this.authService.logout(null).subscribe();
          this.isLoading.set(false);
          this.errorMessage.set('Customer accounts must use Customer Login. This workspace is for admin, officer, and manager users.');
          return;
        }
        this.isLoading.set(false);
        this.authService.navigateAfterLogin(user);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Invalid email or password.');
      }
    });
  }
}
