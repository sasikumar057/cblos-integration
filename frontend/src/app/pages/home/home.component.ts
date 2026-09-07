import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { CustomerService, RegistrationStatus } from '../../core/services/customer.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  private readonly customerService = inject(CustomerService);

  statusEmail = '';
  statusPanelOpen = signal(false);
  workspaceOpen = signal(false);
  checkingStatus = signal(false);
  registrationStatus = signal<RegistrationStatus | null>(null);
  statusError = signal('');

  toggleStatusPanel(): void {
    this.statusPanelOpen.update(open => !open);
  }

  closeStatusPanel(): void {
    this.statusPanelOpen.set(false);
  }

  toggleWorkspace(): void {
    this.workspaceOpen.update(open => !open);
  }

  closeWorkspace(): void {
    this.workspaceOpen.set(false);
  }

  checkRegistrationStatus(): void {
    const email = this.statusEmail.trim();
    this.registrationStatus.set(null);
    this.statusError.set('');

    if (!email) {
      this.statusError.set('Enter the email used during registration.');
      return;
    }

    this.checkingStatus.set(true);
    this.customerService.checkRegistrationStatus(email).subscribe({
      next: status => {
        this.registrationStatus.set(status);
        this.checkingStatus.set(false);
      },
      error: err => {
        this.statusError.set(err?.error?.message || 'Unable to check registration status right now.');
        this.checkingStatus.set(false);
      }
    });
  }

  private readonly router = inject(Router);

redirectToReapply(email: string): void {
  if (!email) return;
  
  // 🟢 Navigates to /register?email=contact@mix.com
  this.router.navigate(['/register'], { 
    queryParams: { email: email.trim() } 
  });
}
}
