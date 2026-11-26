package com.example.bookstore.Service;

public class CultivationHelper {

    public static class RealmInfo {
        public final String name;
        public final String badgeClass;
        public final String nextRealmName;
        public final int progressPercent;
        public final String nextGoalText;

        public RealmInfo(String name, String badgeClass, String nextRealmName, int progressPercent, String nextGoalText) {
            this.name = name;
            this.badgeClass = badgeClass;
            this.nextRealmName = nextRealmName;
            this.progressPercent = progressPercent;
            this.nextGoalText = nextGoalText;
        }
    }

  
    public static RealmInfo calculateRealm(double totalSpent, int totalBooksBought) {
        long points = Math.round(totalSpent / 10000.0) + (totalBooksBought * 5L);

        String[] realms = {
                "Phàm Nhân", "Luyện Khí Kỳ", "Trúc Cơ Kỳ", "Kim Đan Kỳ",
                "Nguyên Anh Kỳ", "Hóa Thần Kỳ", "Luyện Hư Kỳ", "Đại Thừa Kỳ", "Độ Kiếp Phi Thăng"
        };
        long[] thresholds = { 0, 20, 60, 150, 300, 500, 800, 1200, 2000 };
        String[] badges = {
                "bg-secondary", "bg-info text-dark", "bg-primary", "bg-warning text-dark",
                "bg-success", "bg-danger", "bg-dark", "bg-warning text-dark border border-danger", "bg-danger border border-warning"
        };

        int idx = 0;
        for (int i = realms.length - 1; i >= 0; i--) {
            if (points >= thresholds[i]) {
                idx = i;
                break;
            }
        }

        if (idx == realms.length - 1) {
            return new RealmInfo(realms[idx], badges[idx], "Đã đạt cảnh giới tối cao", 100, "Tối đa");
        }

        long currentMin = thresholds[idx];
        long nextMin = thresholds[idx + 1];
        int progress = (int) Math.min(100, Math.max(0, ((points - currentMin) * 100) / (nextMin - currentMin)));
        long needed = nextMin - points;

        return new RealmInfo(
                realms[idx],
                badges[idx],
                realms[idx + 1],
                progress,
                "Cần thêm " + needed + " điểm tu vi để đột phá " + realms[idx + 1]
        );
    }
}