package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    // Méthodes existantes (gardez-les)
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByUser(User user);
    List<Campaign> findByTitleContainingIgnoreCase(String keyword);
    List<Campaign> findByTitleContainingIgnoreCaseAndStatus(String keyword, CampaignStatus status);
    List<Campaign> findByCategory_NameIgnoreCaseAndStatus(String categoryName, CampaignStatus status);
    
    // ========== NOUVELLES MÉTHODES PAGINÉES ==========
    
    // Pagination de base - toutes les campagnes actives
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);
    
    // Pagination avec recherche par titre
    Page<Campaign> findByTitleContainingIgnoreCaseAndStatus(String keyword, CampaignStatus status, Pageable pageable);
    
    // Pagination par catégorie
    Page<Campaign> findByCategory_NameIgnoreCaseAndStatus(String categoryName, CampaignStatus status, Pageable pageable);
    
    // Pagination pour les campagnes d'un utilisateur
    Page<Campaign> findByUser(User user, Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.status = :status " +
       "AND (:q IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%'))) " +
       "AND (:categoryName IS NULL OR LOWER(c.category.name) = LOWER(:categoryName)) " +
       "AND (:location IS NULL OR LOWER(c.location) LIKE LOWER(CONCAT('%', :location, '%')))")
Page<Campaign> searchActive(@Param("status") CampaignStatus status,
                            @Param("q") String q,
                            @Param("categoryName") String categoryName,
                            @Param("location") String location,
                            Pageable pageable);

@Query("SELECT DISTINCT c.location FROM Campaign c WHERE c.status = :status AND c.location IS NOT NULL AND c.location <> '' ORDER BY c.location")
List<String> findDistinctLocationsByStatus(@Param("status") CampaignStatus status);
}