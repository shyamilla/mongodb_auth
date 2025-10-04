import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './welcome.html',
  styleUrls: ['./welcome.css']
})
export class WelcomeComponent implements OnInit {
  username = '';

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
    // const token = localStorage.getItem('token');
    // if (!token) {
    //   this.router.navigate(['/login'])
    // }
    // else {
    //   this.router.navigate(['/welcome'])
    //   return;
    // }

   

  this.authService.getWelcome().subscribe({
    next: (res: any) => {
      this.username = res.username || res;
    },
    error: () => {
      // Token invalid or expired
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  });
}

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
