#!/usr/bin/env python3
"""
Mermaid Diagram Validator
Extrai e valida diagramas Mermaid do README.md
"""

import re
import subprocess
import sys
import tempfile
import os

def extract_mermaid_diagrams(filename):
    """Extrai todos os diagramas Mermaid do arquivo"""
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Regex para encontrar blocos mermaid (non-greedy)
    pattern = r'```mermaid\n(.*?)\n```'
    all_matches = re.findall(pattern, content, re.DOTALL)
    
    # Filtrar diagramas vazios ou muito pequenos
    diagrams = [d.strip() for d in all_matches if d.strip() and len(d.strip()) > 10]
    
    return diagrams

def validate_diagram(diagram_code, diagram_num):
    """Valida um diagrama Mermaid usando mmdc"""
    with tempfile.NamedTemporaryFile(mode='w', suffix='.mmd', delete=False) as f:
        f.write(diagram_code)
        input_file = f.name
    
    output_file = tempfile.mktemp(suffix='.svg')
    
    try:
        result = subprocess.run(
            ['mmdc', '-i', input_file, '-o', output_file],
            capture_output=True,
            text=True,
            timeout=10
        )
        
        os.unlink(input_file)
        if os.path.exists(output_file):
            os.unlink(output_file)
        
        if result.returncode != 0 or 'error' in result.stderr.lower():
            return False, result.stderr
        return True, None
    except Exception as e:
        return False, str(e)
    finally:
        if os.path.exists(input_file):
            os.unlink(input_file)
        if os.path.exists(output_file):
            os.unlink(output_file)

def main():
    print("🔍 Validating Mermaid diagrams in README.md...")
    print()
    
    # Verificar se mmdc está instalado
    try:
        subprocess.run(['mmdc', '--version'], capture_output=True, check=True)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("❌ Mermaid CLI (mmdc) not installed!")
        print("📦 Install with: npm install -g @mermaid-js/mermaid-cli")
        sys.exit(1)
    
    # Extrair diagramas
    diagrams = extract_mermaid_diagrams('README.md')
    print(f"📊 Found {len(diagrams)} Mermaid diagram(s)")
    print()
    
    # Validar cada diagrama
    errors_found = 0
    for i, diagram in enumerate(diagrams, 1):
        valid, error = validate_diagram(diagram, i)
        if valid:
            print(f"✅ Diagram {i} is valid")
        else:
            print(f"❌ Diagram {i} has syntax errors:")
            if error:
                print(f"   {error[:200]}")
            errors_found += 1
    
    print()
    if errors_found > 0:
        print(f"❌ Found {errors_found} diagram(s) with syntax errors!")
        print("💡 Test diagrams at: https://mermaid.live/")
        sys.exit(1)
    else:
        print(f"✅ All {len(diagrams)} Mermaid diagrams have valid syntax!")
        sys.exit(0)

if __name__ == '__main__':
    main()
