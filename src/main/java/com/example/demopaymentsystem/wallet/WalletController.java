package com.example.demopaymentsystem.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/api/wallets")
    public CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request);
    }

    @GetMapping("/api/users/{userId}/wallets")
    public FindWalletResponse findWalletByUserId(@PathVariable Long userId) {
        return walletService.findWalletByUserId(userId);
    }

}
