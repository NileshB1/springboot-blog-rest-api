package com.springboot.blog.service.impl;

import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.repository.PostRespository;
import com.springboot.blog.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private PostRespository postRespository;
    private ModelMapper mapper;

    //@Autowired --Since only one constructor
    public PostServiceImpl(PostRespository postRespository, ModelMapper mapper) {
        this.postRespository = postRespository;
        this.mapper = mapper;
    }

    @Override
    public PostDto createPost(PostDto postDto) {
        //Convert DTO to entity
        Post post = mapToEntity(postDto);

        Post newPost = postRespository.save(post);

        //Convert entity to DTO
        PostDto postResponse = mapToDto(newPost);

        return postResponse;
    }

    @Override
    public PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sortObj = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        List<PostDto> postDtoList = new ArrayList<>();
        Page<Post> posts = postRespository.findAll(pageable);

        //Get content from page object
        List<Post> listOfPost = posts.getContent();

        List<PostDto> contents = listOfPost.stream().map(post -> mapToDto(post)).collect(Collectors.toList());
        PostResponse postResponse = new PostResponse();
        postResponse.setContent(contents);
        postResponse.setPageNo(posts.getNumber());
        postResponse.setPageSize(posts.getSize());
        postResponse.setTotalElements(posts.getTotalElements());
        postResponse.setTotalPages(posts.getTotalPages());
        postResponse.setLast(posts.isLast());
       /* for(Post post: postList) {
            PostDto postDto = mapToDto(post);
            postDtoList.add(postDto);
            return postDtoList;
        }*/
        return postResponse;
    }
    @Override
    public PostDto getPostById(long id) {
        Post post = postRespository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return mapToDto(post);
    }

    @Override
    public PostDto updatePost(PostDto postDto, long id) {
        //Get post entity from DB by id
        Post post = postRespository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setDescription(postDto.getDescription());

        Post updatedPost = postRespository.save(post);
        return mapToDto(updatedPost);
    }

    @Override
    public void deletePostById(long id) {
        //Get a post by Id from database
        Post post = postRespository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        postRespository.delete(post);
    }

    /**
     * Convert entity into DTO
     * @param post
     * @return
     */
    private PostDto mapToDto(Post post) {
        PostDto postDto = mapper.map(post, PostDto.class);
        /*PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setDescription(post.getDescription());
        postDto.setContent(post.getTitle());
        postDto.setId(post.getId());*/
        return postDto;
    }

    /**
     * Convert DTO to entity
     * @param postDto
     * @return
     */
    private Post mapToEntity(PostDto postDto) {
        Post post = mapper.map(postDto, Post.class);
        /*Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setDescription(postDto.getDescription());*/
        return post;
    }
}
