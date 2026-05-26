package com.nerdcoffe.repository;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.User;
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
}
