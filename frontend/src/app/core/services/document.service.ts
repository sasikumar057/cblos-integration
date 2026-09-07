import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DocumentSummary {
  documentId: number;
  applicationId?: number;
  fileName: string;
  fileType?: string;
  documentType?: string;
  uploadDate?: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/documents`;

  upload(applicationId: number, documentType: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('documentType', documentType);
    formData.append('file', file);
    return this.http.post(`${this.base}/upload/${applicationId}`, formData, { responseType: 'text' });
  }

  listByApplication(applicationId: number): Observable<DocumentSummary[]> {
    return this.http.get<DocumentSummary[]>(`${this.base}/application/${applicationId}`);
  }

  download(documentId: number): Observable<Blob> {
    return this.http.get(`${this.base}/download/${documentId}`, { responseType: 'blob' });
  }
}
