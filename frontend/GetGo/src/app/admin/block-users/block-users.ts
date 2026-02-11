import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminNavBarComponent } from '../../layout/admin-nav-bar/admin-nav-bar.component';
import { AdminService, UserEmailDTO, Page } from '../service/admin.service';

@Component({
  selector: 'block-users',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminNavBarComponent],
  templateUrl: './block-users.html',
  styleUrl: './block-users.css',
})
export class BlockUsers implements OnInit {
  unblockedUsers: UserEmailDTO[] = [];
  unblockedSearch: string = '';
  unblockedPage: number = 0;
  unblockedTotalPages: number = 0;
  selectedUserToBlock: UserEmailDTO | null = null;
  blockReason: string = '';

  blockedUsers: UserEmailDTO[] = [];
  blockedSearch: string = '';
  blockedPage: number = 0;
  blockedTotalPages: number = 0;
  selectedUserToUnblock: UserEmailDTO | null = null;

  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUnblockedUsers();
    this.loadBlockedUsers();
  }

  get canBlock(): boolean {
    return this.selectedUserToBlock !== null && this.blockReason.trim().length > 0;
  }

  get canUnblock(): boolean {
    return this.selectedUserToUnblock !== null;
  }

  loadUnblockedUsers(): void {
    this.adminService.getUnblockedUsers(this.unblockedSearch, this.unblockedPage, 3).subscribe({
      next: (page: Page<UserEmailDTO>) => {
        this.unblockedUsers = page.content;
        this.unblockedTotalPages = page.totalPages;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load users';
        this.cdr.detectChanges();
      }
    });
  }

  loadBlockedUsers(): void {
    this.adminService.getBlockedUsers(this.blockedSearch, this.blockedPage, 3).subscribe({
      next: (page: Page<UserEmailDTO>) => {
        this.blockedUsers = page.content;
        this.blockedTotalPages = page.totalPages;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load blocked users';
        this.cdr.detectChanges();
      }
    });
  }

  searchUnblocked(): void {
    this.unblockedPage = 0;
    this.selectedUserToBlock = null;
    this.loadUnblockedUsers();
  }

  searchBlocked(): void {
    this.blockedPage = 0;
    this.selectedUserToUnblock = null;
    this.loadBlockedUsers();
  }

  selectToBlock(user: UserEmailDTO): void {
    this.selectedUserToBlock = user;
  }

  selectToUnblock(user: UserEmailDTO): void {
    this.selectedUserToUnblock = user;
  }

  blockUser(): void {
    if (!this.canBlock) return;

    this.adminService.blockUser(this.selectedUserToBlock!.id, this.blockReason).subscribe({
      next: (res) => {
        this.successMessage = `Blocked ${res.email}`;
        this.errorMessage = null;
        this.selectedUserToBlock = null;
        this.blockReason = '';
        this.loadUnblockedUsers();
        this.loadBlockedUsers();
        this.clearMessage();
      },
      error: () => {
        this.errorMessage = 'Failed to block user';
        this.cdr.detectChanges();
      }
    });
  }

  unblockUser(): void {
    if (!this.canUnblock) return;

    this.adminService.unblockUser(this.selectedUserToUnblock!.id).subscribe({
      next: (res) => {
        this.successMessage = `Unblocked ${res.email}`;
        this.errorMessage = null;
        this.selectedUserToUnblock = null;
        this.loadUnblockedUsers();
        this.loadBlockedUsers();
        this.clearMessage();
      },
      error: () => {
        this.errorMessage = 'Failed to unblock user';
        this.cdr.detectChanges();
      }
    });
  }

  unblockedPrev(): void {
    if (this.unblockedPage > 0) {
      this.unblockedPage--;
      this.selectedUserToBlock = null;
      this.loadUnblockedUsers();
    }
  }

  unblockedNext(): void {
    if (this.unblockedPage < this.unblockedTotalPages - 1) {
      this.unblockedPage++;
      this.selectedUserToBlock = null;
      this.loadUnblockedUsers();
    }
  }

  blockedPrev(): void {
    if (this.blockedPage > 0) {
      this.blockedPage--;
      this.selectedUserToUnblock = null;
      this.loadBlockedUsers();
    }
  }

  blockedNext(): void {
    if (this.blockedPage < this.blockedTotalPages - 1) {
      this.blockedPage++;
      this.selectedUserToUnblock = null;
      this.loadBlockedUsers();
    }
  }

  private clearMessage(): void {
    setTimeout(() => {
      this.successMessage = null;
      this.cdr.detectChanges();
    }, 3000);
  }
}
