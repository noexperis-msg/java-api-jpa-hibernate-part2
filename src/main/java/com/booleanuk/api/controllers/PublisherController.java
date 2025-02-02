package com.booleanuk.api.controllers;

import com.booleanuk.api.models.Publisher;
import com.booleanuk.api.repositories.PublisherRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("publishers")
public class PublisherController {
    private final PublisherRepository repository;

    public PublisherController(PublisherRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Publisher add(@RequestBody Publisher publisher) {
        checkIfValidObject(publisher);
        return this.repository.save(publisher);
    }

    @GetMapping
    public List<Publisher> getAll(@RequestParam(required = false) String location) {
        if (location != null && !location.isBlank()) {
            return this.repository.findAllByLocation(location);
        }
        return this.repository.findAll();
    }

    @GetMapping("{id}")
    public Publisher getById(@PathVariable int id) {
        return this.repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Publisher updateById(@PathVariable int id, @RequestBody Publisher publisher) {
        checkIfValidObject(publisher);
        Publisher publisherToUpdate = this.repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        publisherToUpdate.setName(publisher.getName());
        publisherToUpdate.setLocation(publisher.getLocation());

        return this.repository.save(publisherToUpdate);
    }

    @DeleteMapping("{id}")
    public Publisher deleteById(@PathVariable int id) {
        Publisher publisherToDelete = this.repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            this.repository.delete(publisherToDelete);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete publisher: Publisher exists as FK in one or more books");
        }
        return publisherToDelete;
    }

    private void checkIfValidObject(Publisher publisher) {
        if (Stream.of(publisher.getName(), publisher.getLocation())
                .anyMatch(field -> field == null || field.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not create object. Required fields are NULL or empty.");
        }
    }
}
