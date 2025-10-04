import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import * as jwt_decode from 'jwt-decode';

interface JwtPayload {
  exp?: number;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router) { }

  canActivate(): boolean {
    const token = localStorage.getItem('token');

    if (token) {
      try {
        const decoded: JwtPayload = (jwt_decode as any)(token);
        const now = Math.floor(Date.now() / 1000);

        if (decoded.exp && decoded.exp < now) {
          this.router.navigate(['/login']);
          return false;
        }
        return true;
      } catch (err) {
        this.router.navigate(['/login']);
        return false;
      }
    }

    this.router.navigate(['/login']);
    return false;
  }
}
