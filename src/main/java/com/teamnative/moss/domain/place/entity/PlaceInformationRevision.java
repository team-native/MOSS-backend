package com.teamnative.moss.domain.place.entity;

import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.search.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_information_revisions")
public class PlaceInformationRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserAccount changedBy;

    @Lob
    @Column(name = "previous_information", nullable = false)
    private String previousInformation;

    @Lob
    @Column(name = "updated_information", nullable = false)
    private String updatedInformation;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    protected PlaceInformationRevision() {}

    public PlaceInformationRevision(Place place, UserAccount changedBy, String previousInformation,
                                    String updatedInformation, LocalDateTime changedAt) {
        this.place = place;
        this.changedBy = changedBy;
        this.previousInformation = previousInformation;
        this.updatedInformation = updatedInformation;
        this.changedAt = changedAt;
    }
}
