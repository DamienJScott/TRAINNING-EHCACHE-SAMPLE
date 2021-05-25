import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { JhiEventManager, JhiParseLinks } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IBook } from 'app/shared/model/book.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { BookService } from './book.service';
import { BookDeleteDialogComponent } from './book-delete-dialog.component';
import { FormBuilder } from '@angular/forms';

@Component({
  selector: 'ehc-book',
  templateUrl: './book.component.html',
  styleUrls: ['./book.component.scss'],
})
export class BookComponent implements OnInit, OnDestroy {
  books: IBook[];
  eventSubscriber?: Subscription;
  itemsPerPage: number;
  links: any;
  page: number;
  predicate: string;
  ascending: boolean;
  search: any;

  searchForm = this.fb.group({
    name: [],
    position: [],
    author: [],
    detail: [],
  });

  constructor(
    protected bookService: BookService,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal,
    protected parseLinks: JhiParseLinks,
    protected fb: FormBuilder
  ) {
    this.books = [];
    this.search = {};
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.page = 0;
    this.links = {
      last: 0,
    };
    this.predicate = 'id';
    this.ascending = true;
  }

  resetCondition(): void {
    this.searchForm.reset();
    this.search = {};
    this.reset();
  }

  searching(): void {
    this.search = Object.assign({}, this.searchForm.value);
    this.search.position = this.search.position?.code;
    Object.keys(this.search).forEach(key => {
      if (this.search[key]) {
        this.search[key + '.contains'] = this.search[key];
      }
      Reflect.deleteProperty(this.search, key);
    });
    this.reset();
  }

  loadAll(): void {
    this.search['page'] = this.page;
    this.search['size'] = this.itemsPerPage;
    this.search['sort'] = this.sort();
    this.bookService.query(this.search).subscribe((res: HttpResponse<IBook[]>) => this.paginateBooks(res.body, res.headers));
  }

  reset(): void {
    this.page = 0;
    this.books = [];
    this.loadAll();
  }

  loadPage(page: number): void {
    this.page = page;
    this.loadAll();
  }

  ngOnInit(): void {
    this.loadAll();
    this.registerChangeInBooks();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: IBook): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInBooks(): void {
    this.eventSubscriber = this.eventManager.subscribe('bookListModification', () => this.reset());
  }

  delete(book: IBook): void {
    const modalRef = this.modalService.open(BookDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.book = book;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected paginateBooks(data: IBook[] | null, headers: HttpHeaders): void {
    const headersLink = headers.get('link');
    this.links = this.parseLinks.parse(headersLink ? headersLink : '');
    if (data) {
      for (let i = 0; i < data.length; i++) {
        this.books.push(data[i]);
      }
    }
  }
}
