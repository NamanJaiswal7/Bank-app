import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { AsyncPipe, NgIf, DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

import { TransactionActions } from '../../state/transaction/transaction.actions';
import { selectSelectedTransaction, selectTransactionLoading, selectTransactionError } from '../../state/transaction/transaction.selectors';
import { Transaction } from '../../core/models/bank.models';

@Component({
  selector: 'app-transaction-detail-page',
  standalone: true,
  imports: [AsyncPipe, NgIf, DecimalPipe, DatePipe, RouterLink],
  templateUrl: './transaction-detail-page.component.html',
  styleUrl: './transaction-detail-page.component.scss'
})
export class TransactionDetailPageComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private store = inject(Store);

  transaction$ = this.store.select(selectSelectedTransaction);
  loading$ = this.store.select(selectTransactionLoading);
  error$ = this.store.select(selectTransactionError);

  private routeSub!: Subscription;

  ngOnInit() {
    this.routeSub = this.route.params.subscribe(params => {
      const id = Number(params['id']);
      if (!isNaN(id)) {
        this.store.dispatch(TransactionActions.loadTransactionDetail({ transactionId: id }));
      }
    });
  }

  ngOnDestroy() {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
  }

  exportToPdf(tx: Transaction) {
    const doc = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a5' // compact receipt size
    });

    // Color Palette
    const primaryColor: [number, number, number] = [99, 102, 241]; // Indigo
    const secondaryColor: [number, number, number] = [18, 20, 32]; // Dark Slate
    const greyColor: [number, number, number] = [148, 163, 184]; // Light Grey

    // Logo & Header
    doc.setFillColor(secondaryColor[0], secondaryColor[1], secondaryColor[2]);
    doc.rect(0, 0, 148, 25, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(16);
    doc.text('AURA BANK', 12, 16);

    doc.setFont('Helvetica', 'normal');
    doc.setFontSize(8);
    doc.text('Premium Digital Wealth Platform', 98, 16);

    // Title
    doc.setTextColor(secondaryColor[0], secondaryColor[1], secondaryColor[2]);
    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(14);
    doc.text('TRANSACTION RECEIPT', 12, 40);

    // Divider Line
    doc.setDrawColor(primaryColor[0], primaryColor[1], primaryColor[2]);
    doc.setLineWidth(0.8);
    doc.line(12, 44, 136, 44);

    // Table Content
    const bodyData = [
      ['Receipt Date', new Date().toLocaleDateString()],
      ['Transaction ID', tx.id.toString()],
      ['Associated Account', `#${tx.accountId}`],
      ['Transaction Type', tx.type],
      ['Reference ID', tx.referenceId || 'N/A'],
      ['Description', tx.description || 'Standard operation'],
      ['Execution Date', new Date(tx.timestamp).toLocaleString()],
      ['Status', 'COMPLETED']
    ];

    autoTable(doc, {
      startY: 50,
      margin: { left: 12, right: 12 },
      body: bodyData,
      theme: 'plain',
      styles: {
        fontSize: 9,
        font: 'Helvetica',
        cellPadding: 3
      },
      columnStyles: {
        0: { fontStyle: 'bold', textColor: secondaryColor, cellWidth: 45 },
        1: { textColor: [50, 50, 50] as [number, number, number] }
      }
    });

    // Transaction Amount Banner (centered highlighted box)
    const finalY = (doc as any).lastAutoTable.finalY + 10;
    doc.setFillColor(248, 250, 252);
    doc.rect(12, finalY, 124, 20, 'F');
    doc.setDrawColor(226, 232, 240);
    doc.setLineWidth(0.3);
    doc.rect(12, finalY, 124, 20, 'S');

    doc.setTextColor(secondaryColor[0], secondaryColor[1], secondaryColor[2]);
    doc.setFont('Helvetica', 'bold');
    doc.setFontSize(10);
    doc.text('AMOUNT PROCESSED', 18, finalY + 8);

    doc.setFont('Courier', 'bold');
    doc.setFontSize(14);
    const symbol = this.getCurrencySymbol(tx.currency);
    const sign = tx.type === 'CREDIT' || tx.type === 'EXCHANGE_IN' ? '+' : '-';
    doc.text(`${sign} ${symbol}${tx.amount.toFixed(2)} ${tx.currency}`, 18, finalY + 15);

    // Digital signature & footer
    doc.setTextColor(greyColor[0], greyColor[1], greyColor[2]);
    doc.setFont('Helvetica', 'normal');
    doc.setFontSize(7);
    doc.text('This is a digitally generated document. No signature required.', 12, 200);

    // Save File
    doc.save(`AuraBank-Receipt-${tx.id}.pdf`);
  }

  getCurrencySymbol(currency: string): string {
    switch (currency) {
      case 'EUR': return '€';
      case 'GBP': return '£';
      case 'SEK': return 'kr';
      case 'VND': return '₫';
      default: return '$';
    }
  }

  getCurrencyName(currency: string): string {
    switch (currency) {
      case 'EUR': return 'Euro';
      case 'GBP': return 'British Pound';
      case 'SEK': return 'Swedish Krona';
      case 'VND': return 'Vietnamese Dong';
      default: return 'US Dollar';
    }
  }
}
