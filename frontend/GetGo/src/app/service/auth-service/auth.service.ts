import {Injectable, signal} from '@angular/core';
import {UserRole} from '../../model/user.model';
import {JwtHelperService} from '@auth0/angular-jwt';
import {environment} from '../../../env/environment';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private jwtHelper = new JwtHelperService();
  private TOKEN_KEY = 'authToken';

  private roleSignal = signal<UserRole>(UserRole.Guest);
  public role = this.roleSignal.asReadonly();

  private fullNameSignal = signal<string>('');
  public fullName = this.fullNameSignal.asReadonly();

  private profilePictureSignal = signal<string>('');
  public userProfilePictureUrl = this.profilePictureSignal.asReadonly();

  constructor(private http: HttpClient, private router: Router) {
  const token = this.getToken();
  if (token) {
    this.loadRoleFromToken(token);
  } else {
    this.roleSignal.set(UserRole.Guest);
    this.fullNameSignal.set('');
    this.profilePictureSignal.set('/assets/images/sussy_cat.jpg');
  }
}

  setToken(token: string, persistent: boolean = false) {
    if (persistent) {
      localStorage.setItem(this.TOKEN_KEY, token);
      sessionStorage.removeItem(this.TOKEN_KEY);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
      localStorage.removeItem(this.TOKEN_KEY);
    }

    this.loadRoleFromToken(token);
  }

  logout() {
    this.http.post(`${environment.apiHost}/api/auth/logout`, {}).subscribe({
      next: () => this.finishLogout(),
      error: (err) => {
        console.error('AuthService: logout request failed', err);
        this.finishLogout(); // optional fallback
      }
    });
  }

  private finishLogout() {
    localStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.TOKEN_KEY);

    this.roleSignal.set(UserRole.Guest);
    this.fullNameSignal.set('');
    this.router.navigate(['/']);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }

  private loadRoleFromToken(token: string) {
    try {
      const decoded: any = this.jwtHelper.decodeToken(token);

      // ROLE
      const roleFromToken = decoded?.role;
      let mappedRole: UserRole = UserRole.Guest;
      if (roleFromToken?.toLowerCase() === 'admin') mappedRole = UserRole.Admin;
      else if (roleFromToken?.toLowerCase() === 'driver') mappedRole = UserRole.Driver;
      else if (roleFromToken?.toLowerCase() === 'passenger') mappedRole = UserRole.Passenger;
      this.roleSignal.set(mappedRole);

    } catch (err) {
      console.error('loadRoleFromToken: failed to decode token', err);
      this.roleSignal.set(UserRole.Guest);
    }

    this.fetchUserProfile();
  }

  fetchUserProfile() {
    const token = this.getToken();
    if (!token) return;

    const headers = { Authorization: `Bearer ${token}` };

    this.http.get<{ fullName: string, profilePictureUrl: string }>(
        `${environment.apiHost}/api/users/me`,
        { headers }
    ).subscribe({
      next: (res) => {
        this.fullNameSignal.set(res.fullName || '');

        const pictureUrl = res.profilePictureUrl || '/assets/images/sussy_cat.png';

        this.profilePictureSignal.set(pictureUrl);
        console.log('Profile picture URL:', pictureUrl);
        console.log('Full profile response:', res);
      },
      error: (err) => {
        console.error('fetchUserProfile failed', err);
        this.fullNameSignal.set('');
        this.profilePictureSignal.set('/assets/images/sussy_cat.jpg');
      }
    });
  }

  // Get user ID from JWT
  getUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decoded: any = this.jwtHelper.decodeToken(token);
      return decoded?.userId || decoded?.id || decoded?.sub || null;
    } catch (err) {
      console.error('getUserId: failed to decode token', err);
      return null;
    }
  }

  // Get user email from JWT
  getUserEmail(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decoded: any = this.jwtHelper.decodeToken(token);
      return decoded?.email || decoded?.sub || null;
    } catch (err) {
      console.error('getUserEmail: failed to decode token', err);
      return null;
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  }
}
