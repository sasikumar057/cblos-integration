import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DocumentService } from '../../../core/services/document.service';
import { concatMap, finalize, from, last } from 'rxjs';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './document-upload.component.html'
})
export class DocumentUploadComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly documentService = inject(DocumentService);

  applicationId = 0;
  customerId = 0;
  requiredDocuments = [
    { type: 'TAX_RETURN', label: 'Tax Return', help: 'Latest business tax return or filed acknowledgement.' },
    { type: 'BUSINESS_LICENSE', label: 'Business License', help: 'Valid trade license, incorporation certificate, or operating permit.' }
  ];
  selectedFiles: Record<string, File | null> = {
    TAX_RETURN: null,
    BUSINESS_LICENSE: null
  };
  fileNames = signal<Record<string, string>>({});
  error = signal('');
  loading = signal(false);

  ngOnInit(): void {
    this.applicationId = Number(this.route.snapshot.paramMap.get('applicationId'));
    this.customerId = this.auth.currentUser()?.corporateCustomerId ?? 0;
  }

  onFileSelected(type: string, event: Event): void {
    this.error.set('');
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.selectedFiles[type] = null;
      this.fileNames.update(names => ({ ...names, [type]: '' }));
      return;
    }

    const validationError = this.validateFile(file);
    if (validationError) {
      this.selectedFiles[type] = null;
      this.fileNames.update(names => ({ ...names, [type]: '' }));
      input.value = '';
      this.error.set(validationError);
      return;
    }

    this.selectedFiles[type] = file;
    this.fileNames.update(names => ({ ...names, [type]: file.name }));
  }

  upload(): void {
    const missing = this.requiredDocuments.filter(doc => !this.selectedFiles[doc.type]);
    if (missing.length > 0) {
      this.error.set(`Please upload: ${missing.map(doc => doc.label).join(', ')}.`);
      return;
    }
    this.error.set('');
    this.loading.set(true);
    from(this.requiredDocuments).pipe(
      concatMap(doc => this.documentService.upload(this.applicationId, doc.type, this.selectedFiles[doc.type]!)),
      last(),
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: () => this.router.navigate(['/customer/dashboard', this.customerId, 'application-complete']),
      error: () => this.error.set('Upload failed. Please check the selected files and try again.')
    });
  }

  isReady(): boolean {
    return this.requiredDocuments.every(doc => !!this.selectedFiles[doc.type]);
  }

  private validateFile(file: File): string {
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSizeMb = 5;
    if (!allowedTypes.includes(file.type)) {
      return 'Documents must be PDF, JPG, or PNG files.';
    }
    if (file.size > maxSizeMb * 1024 * 1024) {
      return `Each document must be ${maxSizeMb}MB or smaller.`;
    }
    return '';
  }
}
