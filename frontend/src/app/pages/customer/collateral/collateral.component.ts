import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CollateralService } from '../../../core/services/collateral.service';
import { DocumentService } from '../../../core/services/document.service';
import { AuthService } from '../../../core/services/auth.service';
import { finalize, switchMap } from 'rxjs';

@Component({
  selector: 'app-collateral',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './collateral.component.html'
})
export class CollateralComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly collateralService = inject(CollateralService);
  private readonly documentService = inject(DocumentService);
  private readonly fb = inject(FormBuilder);

  applicationId = 0;
  customerId = 0;
  error = signal('');
  fileName = signal('');
  loading = signal(false);
  collateralProofFile: File | null = null;

  form = this.fb.group({
    collateralType: ['REAL_ESTATE', Validators.required],
    estimatedValue: [null as number | null, [Validators.required, Validators.min(1)]],
    assetReferenceNumber: ['', Validators.required],
    description: [''],
    verificationStatus: ['PENDING']
  });

  ngOnInit(): void {
    this.applicationId = Number(this.route.snapshot.paramMap.get('applicationId'));
    this.customerId = this.auth.currentUser()?.corporateCustomerId ?? 0;
  }

  onProofSelected(event: Event): void {
    this.error.set('');
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.collateralProofFile = null;
      this.fileName.set('');
      return;
    }

    const validationError = this.validateFile(file);
    if (validationError) {
      this.collateralProofFile = null;
      this.fileName.set('');
      input.value = '';
      this.error.set(validationError);
      return;
    }

    this.collateralProofFile = file;
    this.fileName.set(file.name);
  }

  submit(): void {
    if (this.form.invalid) return;
    if (!this.collateralProofFile) {
      this.error.set('Please upload collateral proof before continuing.');
      return;
    }

    this.error.set('');
    this.loading.set(true);
    this.collateralService.add(this.applicationId, this.form.value as any).pipe(
      switchMap(() => this.documentService.upload(this.applicationId, 'COLLATERAL_PROOF', this.collateralProofFile!)),
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: () => this.router.navigate(['/customer/dashboard/application', this.applicationId, 'document']),
      error: () => this.error.set('Failed to save collateral or upload collateral proof.')
    });
  }

  private validateFile(file: File): string {
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSizeMb = 5;
    if (!allowedTypes.includes(file.type)) {
      return 'Collateral proof must be a PDF, JPG, or PNG file.';
    }
    if (file.size > maxSizeMb * 1024 * 1024) {
      return `Collateral proof must be ${maxSizeMb}MB or smaller.`;
    }
    return '';
  }
}
