#!/bin/bash
# mermaid-validator.sh - Valida diagramas Mermaid no README.md

echo "🔍 Validating Mermaid diagrams in README.md..."

# Verificar se README.md existe
if [ ! -f "README.md" ]; then
    echo "❌ README.md not found!"
    exit 1
fi

# Contar diagramas Mermaid encontrados
diagram_count=$(grep -c "\`\`\`mermaid" README.md)

if [ "$diagram_count" -eq 0 ]; then
    echo "ℹ️  No Mermaid diagrams found in README.md"
    exit 0
fi

echo "📊 Found $diagram_count Mermaid diagram(s) in README.md"

# Listar posições dos diagramas
echo ""
echo "📍 Diagram locations:"
grep -n "\`\`\`mermaid" README.md | while read -r line; do
    line_num=$(echo "$line" | cut -d: -f1)
    echo "   • Line $line_num: Mermaid diagram found"
done

echo ""

# Verificar se mermaid-cli está instalado
if command -v mmdc &> /dev/null; then
    echo "🔧 Mermaid CLI detected - Running syntax validation..."
    echo ""
    
    # Criar diretório temporário
    temp_dir=$(mktemp -d)
    trap "rm -rf $temp_dir" EXIT
    
    # Extrair cada diagrama individualmente e validar
    diagram_num=0
    errors_found=0
    
    awk '/```mermaid/,/```/' README.md | awk '
        /```mermaid/ { in_diagram=1; diagram=""; next }
        /```/ && in_diagram { 
            print diagram > "/tmp/diagram_" NR ".mmd"
            in_diagram=0
            next 
        }
        in_diagram { diagram = diagram $0 "\n" }
    '
    
    # Validar cada arquivo de diagrama
    for diagram_file in /tmp/diagram_*.mmd; do
        if [ -f "$diagram_file" ]; then
            diagram_num=$((diagram_num + 1))
            
            # Tentar validar o diagrama
            if mmdc -i "$diagram_file" -o "$temp_dir/output_${diagram_num}.svg" 2>&1 | grep -qi "error"; then
                echo "❌ Diagram $diagram_num has syntax errors"
                errors_found=$((errors_found + 1))
            else
                echo "✅ Diagram $diagram_num is valid"
            fi
            
            rm -f "$diagram_file"
        fi
    done
    
    echo ""
    if [ $errors_found -gt 0 ]; then
        echo "❌ Found $errors_found diagram(s) with syntax errors!"
        echo "💡 Test diagrams at: https://mermaid.live/"
        exit 1
    else
        echo "✅ All $diagram_num Mermaid diagrams have valid syntax!"
    fi
else
    echo "⚠️  Mermaid CLI (mmdc) not installed - Skipping syntax validation"
    echo ""
    echo "📦 To install Mermaid CLI for syntax validation:"
    echo "   npm install -g @mermaid-js/mermaid-cli"
    echo ""
    echo "✅ Basic validation complete (location check only)"
fi

echo ""
echo "💡 Manual validation recommended:"
echo "   • Copy diagram code to: https://mermaid.live/"
echo "   • Check GitHub PR preview for rendering"
echo "   • Ensure diagrams render correctly"
echo ""
echo "🎯 All diagrams should render correctly in GitHub!"
