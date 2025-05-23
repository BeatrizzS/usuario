package com.beawata.usuario.business;

import com.beawata.usuario.UsuarioApplication;
import com.beawata.usuario.business.converter.UsuarioConverter;
import com.beawata.usuario.business.dto.EnderecoDTO;
import com.beawata.usuario.business.dto.TelefoneDTO;
import com.beawata.usuario.business.dto.UsuarioDTO;
import com.beawata.usuario.infrastructure.entity.Endereco;
import com.beawata.usuario.infrastructure.entity.Telefone;
import com.beawata.usuario.infrastructure.entity.Usuario;
import com.beawata.usuario.infrastructure.exceptions.ConflictException;
import com.beawata.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.beawata.usuario.infrastructure.repository.EnderecoRepository;
import com.beawata.usuario.infrastructure.repository.TelefoneRepository;
import com.beawata.usuario.infrastructure.repository.UsuarioRepository;
import com.beawata.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioConverter usuarioConverter;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final EnderecoRepository enderecoRepository;

    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario  usuario = usuarioConverter.paraUsuario(usuarioDTO);
         usuario = usuarioRepository.save(usuario);
        return usuarioConverter.paraUsuarioDTO(usuario);
    }

    public void emailExiste(String email){
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw new ConflictException("Email já cadastrado " + email);
            }
        }catch (ConflictException e){
            throw new ConflictException("Email já cadastrado" + e.getCause());
        }
    }
    public boolean verificaEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        try {
        return usuarioConverter.paraUsuarioDTO(
                usuarioRepository.findByEmail(email)
                        .orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado " + email)));
    } catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Email não encontrado " + email);
        }
    }

    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){

        //Aqui buscamos o email do usuário atraves do token (para tirar a
        // obrigatoriedade do email)
        String email = jwtUtil.extractUsername(token.substring(7));


        //Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        //Buscar os dados do usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));

        //Mesclamos os dados que recebemos na requisição DTO com os dados do banco de dados
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        //Salvou os dados do usuario convertido e depois pegou o retorno e converteu para UsuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){
        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto){

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado " + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());
        Endereco enderecoEntity = enderecoRepository.save(endereco);
        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        String email = jwtUtil.extractUsername(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado " + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        //também pode ser feito dessa forma para poupar uma linha do código
        // é a mesma coisa que o "Telefone telefoneEntity = telefoneRepository.save(telefone);"
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

}
