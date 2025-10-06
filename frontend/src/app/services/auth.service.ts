import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }


   getWelcome(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.get(`${this.apiUrl}/welcome`, { headers });
  }


  forgotPassword(body: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot`, body);
  }

  resetPassword(body: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset`, body);
  }

  logout() {
    localStorage.removeItem('token');
  }
}
