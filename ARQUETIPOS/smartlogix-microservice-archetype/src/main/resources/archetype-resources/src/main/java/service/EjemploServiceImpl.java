package ${package}.${artifactId}.service;

import org.springframework.stereotype.Service;

@Service
public class EjemploServiceImpl implements EjemploService {

    @Override
    public String obtenerEstado() {
        return "${serviceName} funcionando correctamente";
    }
}
