package repository;

public class PostDonationCounsellingNamedQueryConstants {
    
    public static final String NAME_FIND_FLAGGED_POST_DONATION_COUNSELLING_FOR_DONOR =
            "PostDonationCounselling.findFlaggedPostDonationCounsellingForDonor";
    public static final String QUERY_FIND_FLAGGED_POST_DONATION_COUNSELLING_FOR_DONOR =
            "SELECT pdc " +
            "FROM PostDonationCounselling pdc " +
            "WHERE pdc.donation.donor = :donor " +
            "AND pdc.flaggedForCounselling = :flaggedForCounselling " +
            "ORDER BY pdc.donation.donationDate ";

}
