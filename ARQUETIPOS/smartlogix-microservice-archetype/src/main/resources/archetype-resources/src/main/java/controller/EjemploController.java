package ${package}.${artifactId}.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/${artifactId}")
public class EjemploController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("${serviceName} operativo");
    }
}
