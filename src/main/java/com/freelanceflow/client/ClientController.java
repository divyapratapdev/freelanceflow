package com.freelanceflow.client;

import com.freelanceflow.client.dto.ClientRequest;
import com.freelanceflow.client.dto.ClientResponse;
import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.PageResponse;
import com.freelanceflow.common.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Manage freelancer clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @RequestBody @Valid ClientRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ClientResponse response = clientService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List all clients (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> listAll(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<ClientResponse> page = PageResponse.of(clientService.listAll(userId, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        ClientResponse response = clientService.getById(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid ClientRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ClientResponse response = clientService.update(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete client")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        clientService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
