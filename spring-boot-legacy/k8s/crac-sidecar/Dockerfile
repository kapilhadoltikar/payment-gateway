FROM alpine:3.19

# Install required tools
RUN apk add --no-cache bash curl jq

# Create checkpoint directory
RUN mkdir -p /crac-checkpoint

# Copy checkpoint manager script
COPY checkpoint-manager.sh /usr/local/bin/checkpoint-manager.sh
RUN chmod +x /usr/local/bin/checkpoint-manager.sh

# Set working directory
WORKDIR /crac-checkpoint

# Run checkpoint manager
ENTRYPOINT ["/usr/local/bin/checkpoint-manager.sh"]
