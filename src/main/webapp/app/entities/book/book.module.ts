import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { EhcMonolithSampleSharedModule } from 'app/shared/shared.module';
import { BookComponent } from './book.component';
import { BookDetailComponent } from './book-detail.component';
import { BookUpdateComponent } from './book-update.component';
import { BookDeleteDialogComponent } from './book-delete-dialog.component';
import { bookRoute } from './book.route';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

@NgModule({
  imports: [EhcMonolithSampleSharedModule, CardModule, ButtonModule, InputTextModule, RouterModule.forChild(bookRoute)],
  declarations: [BookComponent, BookDetailComponent, BookUpdateComponent, BookDeleteDialogComponent],
  entryComponents: [BookDeleteDialogComponent],
})
export class EhcMonolithSampleBookModule {}
