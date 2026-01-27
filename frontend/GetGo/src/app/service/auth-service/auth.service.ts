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
  public logoutInProgress = false;

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
    let isDriver = false;
    try {
      const decoded: any = this.jwtHelper.decodeToken(token);
      const roleFromToken = decoded?.role?.toLowerCase?.() || '';
      isDriver = roleFromToken === 'driver';
    } catch (e) {
      console.error('setToken: failed to decode token when deciding storage location', e);
    }

    /*if (isDriver) {
      localStorage.setItem(this.TOKEN_KEY, token);
      sessionStorage.removeItem(this.TOKEN_KEY);
    } else {*/
      if (!persistent) {
        sessionStorage.setItem(this.TOKEN_KEY, token);
        localStorage.removeItem(this.TOKEN_KEY);
      } else {
        localStorage.setItem(this.TOKEN_KEY, token);
        sessionStorage.removeItem(this.TOKEN_KEY);
      /*}*/
    }

    this.loadRoleFromToken(token);
  }

  logout() {
    if (this.logoutInProgress) {
      console.log('AuthService: logout already in progress, ignoring');
      return;
    }

    console.log('AuthService: logging out user');
    this.logoutInProgress = true;

    this.http.post<boolean>(`${environment.apiHost}/api/auth/logout`, {}).subscribe({
      next: (allowed) => {
        this.logoutInProgress = false;

        if (allowed) {
          console.log('AuthService: logout allowed by server, clearing session');
          this.clearSession();
          this.router.navigate(['/home']);
        } else {
          console.warn('AuthService: logout NOT allowed by server (e.g. active driver). Session kept.');
        }
      },
      error: (err) => {
        this.logoutInProgress = false;

        console.error('AuthService: logout request failed', err);

        if (err.status === 401) {
          console.log('AuthService: server reports unauthorized, clearing local session');
          this.clearSession();
          this.router.navigate(['/home']);
        } else {
          this.router.navigate(['/home']);
        }
      }
    });
  }

  clearSession() {
    console.log('AuthService: clearing session');
    localStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.TOKEN_KEY);

    this.roleSignal.set(UserRole.Guest);
    this.fullNameSignal.set('');
    this.profilePictureSignal.set('/assets/images/sussy_cat.jpg');
  }

  private finishLogout() {
    this.clearSession();
    this.router.navigate(['/']);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
    return !!token && !this.jwtHelper.isTokenExpired(token);
  }

  private loadRoleFromToken(token: string) {
    try {
      const decoded: any = this.jwtHelper.decodeToken(token);

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
