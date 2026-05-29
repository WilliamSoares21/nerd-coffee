package com.nerdcoffe.repository;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.dto.TagDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    @Query("SELECT a FROM Article a WHERE a.published = true ORDER BY a.publishedAt DESC")
    Page<Article> findAllPublished(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.author = :author ORDER BY a.createdAt DESC")
    Page<Article> findByAuthor(@Param("author") User author, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:title% AND a.published = true")
    Page<Article> searchPublishedByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT a FROM Article a LEFT JOIN a.upvotes u WHERE a.published = true GROUP BY a ORDER BY COUNT(u) * 2 DESC, a.publishedAt DESC")
    Page<Article> findTrending(Pageable pageable);

    @Query("SELECT new com.nerdcoffe.dto.TagDto(t, COUNT(a.id)) FROM Article a JOIN a.tags t WHERE a.published = true GROUP BY t ORDER BY COUNT(a.id) DESC")
    List<TagDto> findPopularTags();
}
