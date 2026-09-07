import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);

  resetForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  });

  isLoading = signal(false);
  message = signal('');
  errorMessage = signal('');

  onSubmit(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    const { email, newPassword, confirmPassword } = this.resetForm.value;
    this.message.set('');
    this.errorMessage.set('');

    if (newPassword !== confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    this.isLoading.set(true);
    this.customerService.resetPassword(email!, newPassword!).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.message.set('Password updated. You can now login with the new password.');
        this.resetForm.reset();
      },
      error: err => {
        this.isLoading.set(false);
        this.errorMessage.set(err?.error?.message || err?.error || 'Password reset failed. Check the customer email and try again.');
      }
    });
  }
}
