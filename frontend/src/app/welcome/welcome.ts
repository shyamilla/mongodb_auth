import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './welcome.html'
})
export class WelcomeComponent implements OnInit {
  username: string | null = null;
  message = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.authService.getWelcome().subscribe({
      next: (res: any) => {
        this.message = res.message || 'Welcome!';
        if (res.username) {
          this.username = res.username;
        }
      },
      error: (err: any) => {
        this.message = 'Unauthorized or session expired';
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
