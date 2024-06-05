package com.relic.relic.service;

import com.relic.relic.dto.AdminBidListDto;
import com.relic.relic.dto.BidDto;
import com.relic.relic.dto.BidListDto;
import com.relic.relic.dto.UserBiddingListDto;

import java.text.ParseException;
import java.util.List;

public interface BiddingService {
    String placeBid(Long productId, BidDto bidDto);
    List<BidListDto> getBiddingList(Long productId);
    BidListDto getWinningBid(Long productId);
    String closeBidding(Long productId);
    String reopenBidding(Long productId) throws ParseException;
    String declareWinner(Long productId);
    List<UserBiddingListDto> getUserBids(Long userId, String keyword, String createdAtStart, String createdAtEnd, String sortBy);
    List<AdminBidListDto> getAllBidsForAdmin(String keyword, String createdAtStart, String createdAtEnd, String sortBy);
}
