package com.balatro.api.filter;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;
import com.balatro.enums.Voucher;

public record VoucherFilter(Voucher voucher, int ante) implements Filter {

    public VoucherFilter(Voucher voucher) {
        this(voucher, -1);
    }

    @Override
    public boolean filter(Run run) {
        if (ante == -1) {
            return run.hasVoucher(voucher);
        }
        return run.getAnte(ante)
                .hasVoucher(voucher);
    }

    @Override
    public void configure(Balatro balatro) {
        balatro.enableVouchers();
    }
}
