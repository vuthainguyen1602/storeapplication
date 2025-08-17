package com.example.storeapplication.service.impl;

import com.example.storeapplication.domain.Basket;
import com.example.storeapplication.domain.BasketItem;
import com.example.storeapplication.domain.Deal;
import com.example.storeapplication.dto.ReceiptResponse;
import com.example.storeapplication.repository.DealRepository;
import com.example.storeapplication.service.ReceiptCalculationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReceiptCalculationServiceImpl implements ReceiptCalculationService {

   private DealRepository dealRepository;

    /**
     * @param basket
     * @return
     */
    @Override
    public ReceiptResponse calculateReceipt(Basket basket) {
        ReceiptResponse receipt = new ReceiptResponse();
        receipt.setSessionId(basket.getSessionId());

        List<ReceiptResponse.ReceiptItem> receiptItems = new ArrayList<>();
        List<ReceiptResponse.AppliedDeal> appliedDeals = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // Calculate subtotal and create receipt items
        for (BasketItem item : basket.getItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            receiptItems.add(new ReceiptResponse.ReceiptItem(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    itemTotal
            ));
        }

        // Apply deals
        for (BasketItem item : basket.getItems()) {
            List<Deal> activeDeals = dealRepository.findActiveDealsForProduct(
                    item.getProduct().getId(),
                    LocalDateTime.now()
            );

            for (Deal deal : activeDeals) {
                if (!deal.isExpired()) {
                    BigDecimal discount = calculateDiscount(item, deal);
                    if (discount.compareTo(BigDecimal.ZERO) > 0) {
                        totalDiscount = totalDiscount.add(discount);
                        appliedDeals.add(new ReceiptResponse.AppliedDeal(
                                deal.getDescription(),
                                discount
                        ));
                    }
                }
            }
        }

        BigDecimal totalPrice = subtotal.subtract(totalDiscount);

        receipt.setItems(receiptItems);
        receipt.setAppliedDeals(appliedDeals);
        receipt.setSubtotal(subtotal);
        receipt.setTotalDiscount(totalDiscount);
        receipt.setTotalPrice(totalPrice.max(BigDecimal.ZERO));

        return receipt;
    }

    /**
     * @param item
     * @param deal
     * @return
     */
    @Override
    public BigDecimal calculateDiscount(BasketItem item, Deal deal) {
        if (item.getQuantity() >= deal.getBuyQuantity()) {
            BigDecimal basePrice = item.getUnitPrice();

            if (deal.getDiscountPercentage() != null) {
                // Percentage discount
                int applicableSets = item.getQuantity() / deal.getBuyQuantity();
                int discountQuantity = deal.getGetQuantity() != null ?
                        deal.getGetQuantity() * applicableSets : applicableSets;

                BigDecimal discountAmount = basePrice
                        .multiply(deal.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(Math.min(discountQuantity, item.getQuantity())));

                return discountAmount;
            }

            if (deal.getDiscountAmount() != null) {
                // Fixed amount discount
                int applicableSets = item.getQuantity() / deal.getBuyQuantity();
                return deal.getDiscountAmount().multiply(BigDecimal.valueOf(applicableSets));
            }
        }

        return BigDecimal.ZERO;
    }
}
