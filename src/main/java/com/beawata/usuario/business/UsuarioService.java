package com.beawata.usuario.business;

import com.beawata.usuario.business.converter.UsuarioConverter;
import com.beawata.usuario.business.dto.UsuarioDTO;
import com.beawata.usuario.infrastructure.entity.Usuario;
import com.beawata.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        //para simplicar as duas linhas de baixo é só levar o usuario.Repository para o return
        //usuario = usuarioRepository.save(usuario);
        //return usuarioConverter.paraUsuarioDTO(usuario);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));

    }
}
